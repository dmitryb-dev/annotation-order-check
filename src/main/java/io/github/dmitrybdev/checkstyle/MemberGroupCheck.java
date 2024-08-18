package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

public class MemberGroupCheck extends AbstractCheck {

    private Collection<ExpectedGroup> groups = List.of();

    private int memberInterval = 1;
    private int singleLineMemberInterval = 0;
    private int groupInterval = 2;


    public void setGroups(String groupsTemplate) {
        int order = 0;
        Collection<ExpectedGroup> groups = new ArrayList<>();
        for (String group : groupsTemplate.split("\\s{3,}")) {

            List<ExpectedMember> modifiersAlternatives = new ArrayList<>();
            for (String modifiersAlternative : group.split(",")) {
                Set<String> requiredModifiers = stream(modifiersAlternative.split("\\s+"))
                        .filter(not(String::isBlank))
                        .map(String::trim)
                        .collect(toCollection(LinkedHashSet::new));
                modifiersAlternatives.add(new ExpectedMember(requiredModifiers));
            }

            groups.add(new ExpectedGroup(modifiersAlternatives, order++));
        }
        this.groups = groups;
    }

    public void setGroupInterval(int groupInterval) {
        this.groupInterval = groupInterval;
    }

    public void setMemberInterval(int memberInterval) {
        this.memberInterval = memberInterval;
    }

    public void setSingleLineMemberInterval(int singleLineMemberInterval) {
        this.singleLineMemberInterval = singleLineMemberInterval;
    }


    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.CLASS_DEF, INTERFACE_DEF };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[0];
    }


    @Override
    public void visitToken(DetailAST ast) {
        DetailAST members = ast.findFirstToken(TokenTypes.OBJBLOCK).getFirstChild();

        ExpectedGroup lastFoundGroup = new ExpectedGroup(List.of(), -1);
        int foundHelpers = 0;

        for (DetailAST member = members; member != null; member = member.getNextSibling()) {
            if (!Set.of(VARIABLE_DEF, CTOR_DEF, METHOD_DEF).contains(member.getType())) continue;
            if (member.getParent().getParent().getType() != CLASS_DEF) continue;

            Set<String> modifiers = getModifiers(member);

            ExpectedGroup startsGroup = getStartsGroup(modifiers);
            boolean isHelper = lastFoundGroup.matchesHelper(modifiers);
            int interval = getInterval(member);

            // When multiple members belongs to the same group -> require memberInterval
            if (foundHelpers == 0 && startsGroup == lastFoundGroup) startsGroup = null;

            foundHelpers = isHelper && interval == getExpectedMemberInterval(member)
                    ? foundHelpers + 1
                    : 0;

            requireInterval(member, startsGroup, isHelper);

            if (startsGroup != null) {
                if (startsGroup.order() < lastFoundGroup.order()) {
                    log(member.getLineNo(), member.getColumnNo(),
                            "'{0}' members group must be placed before '{1}' group",
                            startsGroup.toString(), lastFoundGroup.toString());
                }
                lastFoundGroup = startsGroup;
            }
        }
    }


    private @Nullable MemberGroupCheck.ExpectedGroup getStartsGroup(Set<String> modifiers) {
        return groups.stream()
                .filter(group -> group.matchesGroupStartingMember(modifiers))
                .findFirst().orElse(null);
    }


    private Set<String> getModifiers(DetailAST ast) {
        DetailAST modifiers = ast.findFirstToken(MODIFIERS).getFirstChild();

        Set<String> modifiersSet = Stream.iterate(modifiers, Objects::nonNull, DetailAST::getNextSibling)
                .map(modifier -> modifier.getType() == ANNOTATION
                        ? "@" + FullIdent.createFullIdent(modifier.getFirstChild().getNextSibling()).getText()
                        : modifier.getText()
                )
                .collect(toSet());

        if (ast.getType() == VARIABLE_DEF) modifiersSet.add("<field>");
        if (ast.getType() == CTOR_DEF) modifiersSet.add("<method>");
        if (ast.getType() == METHOD_DEF) modifiersSet.add("<method>");

        System.out.println(modifiersSet);
        return modifiersSet;
    }


    private int getExpectedMemberInterval(DetailAST member) {
        DetailAST prevMember = member.getPreviousSibling();
        if (isSingleLine(member) && (prevMember == null || isSingleLine(prevMember))) return singleLineMemberInterval;
        return memberInterval;
    }

    private int getInterval(DetailAST member) {
        int spaces = 0;
        for (int i = 0; i < member.getLineNo(); i++) {
            String line = getLine(member.getLineNo() - i - 2).trim();
            System.out.println((member.getLineNo() - i - 2) + " " + line);

            if (line.startsWith("//") || line.startsWith("/*") || line.startsWith("*")) continue;
            if (line.isBlank()) {
                spaces++;
                continue;
            }
            if (line.startsWith("{")) spaces++;
            break;
        }
        return spaces;
    }

    private boolean isSingleLine(DetailAST member) {
        if (member.getType() == LCURLY) {
            return member.getLineNo() - member.getParent().getParent().getLineNo() == 0;
        } else {
            return member.getLastChild().getLineNo() - member.getLineNo() == 0;
        }
    }

    private void requireInterval(
            DetailAST member,
            @Nullable ExpectedGroup startsGroup, boolean isHelper
    ) {
        int interval = getInterval(member);
        int expectedMemberInterval = getExpectedMemberInterval(member);

        if (startsGroup != null) {
            if (interval == groupInterval) return;
            if (isHelper) {
                log(member.getLineNo(), member.getColumnNo(),
                        "Member groups must be separated by {0} or {1} line(s). Current interval: {1} line(s)",
                        expectedMemberInterval, groupInterval, interval);
            } else {
                log(member.getLineNo(), member.getColumnNo(),
                        "Member groups must be separated by {0} line(s). Current interval: {1} line(s)",
                        groupInterval, interval);
            }
        } else {
            if (interval == expectedMemberInterval) return;
            if (expectedMemberInterval == memberInterval) {
                log(member.getLineNo(), member.getColumnNo(),
                        "Members must be separated by {0} line(s). Current interval: {1} line(s)",
                        expectedMemberInterval, interval);
            } else {
                log(member.getLineNo(), member.getColumnNo(),
                        "Single-line members must be separated by {0} line(s). Current interval: {1} line(s)",
                        expectedMemberInterval, interval);
            }
        }
    }


    private record ExpectedGroup(Collection<ExpectedMember> allowedMembers, int order) {

        @Override
        public String toString() {
            return allowedMembers.stream()
                    .map(ExpectedMember::toString)
                    .collect(joining(", "));
        }

        public boolean matchesGroupStartingMember(Set<String> modifiers) {
            return allowedMembers.stream()
                    .findFirst()
                    .filter(expectedMember -> expectedMember.matches(modifiers))
                    .isPresent();
        }

        public boolean matchesHelper(Set<String> modifiers) {
            return allowedMembers.stream()
                    .skip(1)
                    .anyMatch(expectedMember -> expectedMember.matches(modifiers));
        }
    }

    private record ExpectedMember(Set<String> expectedModifiers) {

        @Override
        public String toString() {
            return String.join(" ", expectedModifiers);
        }

        public boolean matches(Set<String> actualModifiers) {
            return expectedModifiers.containsAll(actualModifiers);
        }
    }
}
