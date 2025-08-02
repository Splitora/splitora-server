package com.satwik.splitora.service.implementations;

import com.satwik.splitora.constants.ErrorMessages;
import com.satwik.splitora.exception.BadRequestException;
import com.satwik.splitora.exception.DataNotFoundException;
import com.satwik.splitora.persistence.dto.expense.ExpenseListDTO;
import com.satwik.splitora.persistence.dto.group.*;
import com.satwik.splitora.persistence.dto.user.PhoneDTO;
import com.satwik.splitora.persistence.dto.user.UserDTO;
import com.satwik.splitora.persistence.entities.*;
import com.satwik.splitora.repository.*;
import com.satwik.splitora.service.interfaces.GroupService;
import com.satwik.splitora.settlement.model.Transaction;
import com.satwik.splitora.settlement.strategy.SettlementStrategy;
import com.satwik.splitora.settlement.strategy.SortSettlementStrategy;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GroupServiceImpl implements GroupService {

    private final AuthorizationService authorizationService;

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    private final ExpenseRepository expenseRepository;

    private final GroupMembersRepository groupMembersRepository;

    private final UnregisteredUserRepository unregisteredUserRepository;

    public GroupServiceImpl (
            AuthorizationService authorizationService,
            GroupRepository groupRepository,
            UserRepository userRepository,
            ExpenseRepository expenseRepository,
            GroupMembersRepository groupMembersRepository,
            UnregisteredUserRepository unregisteredUserRepository) {
        this.authorizationService = authorizationService;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.groupMembersRepository = groupMembersRepository;
        this.unregisteredUserRepository = unregisteredUserRepository;
    }


    @Override
    @Transactional
    public String createGroup(GroupDTO groupDTO) {
        User user = authorizationService.getAuthorizedUser();
        Group group = new Group();
        group.setGroupName(groupDTO.getGroupName());
        group.setUser(user);
        group.setDefaultGroup(false);
        Group savedGroup = groupRepository.save(group);

        // adding user as the first member of the group
        GroupMembers groupMembers = new GroupMembers();
        groupMembers.setGroup(savedGroup);
        groupMembers.setMember(user);
        groupMembers.setName(user.getUsername());
        groupMembersRepository.save(groupMembers);

        return "Group created successfully!";
    }

    @Override
    public GroupListDTO findAllGroup() {
        User user = authorizationService.getAuthorizedUser();
        List<Group> groupList = groupRepository.findByUserId(user.getId());
        GroupListDTO groupListDTO = new GroupListDTO();
        groupListDTO.setOwner(user.getUsername());
        List<GroupListDTOWithin> groupListDTOS = new ArrayList<>();
        for (Group group : groupList) {
            groupListDTOS.add(new GroupListDTOWithin(group.getId(), group.getGroupName()));
        }
        groupListDTO.setGroups(groupListDTOS);

        return groupListDTO;
    }

    /**
     * Adds a member to the group. If the member is not registered, they will be added as an unregistered user.
     * If the member is already registered or unregistered, they will be added to the group.
     *
     * @param groupId          The ID of the group to which the member is being added.
     * @param addMemberRequest The details of the member to be added.
     * @return A success message indicating that the member was added successfully.
     */
    @Override
    @Transactional
    @PreAuthorize("@authorizationService.isGroupOwner(#groupId)")
    public String addGroupMembers(UUID groupId, GroupMemberDTO addMemberRequest) {

        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DataNotFoundException(ErrorMessages.GROUP_NOT_FOUND));
        // Check if the group is a default group
        if(group.isDefaultGroup()) {
            throw new BadRequestException("You can't add members to the default group!");
        }

        // check for contact number or email in the request
        checkForContactOrEmail(addMemberRequest);

        // Check if the user is already registered or unregistered
        Optional<User> user = userRepository.findByEmailOrPhone(
                addMemberRequest.getEmail(),
                addMemberRequest.getPhone() != null ? addMemberRequest.getPhone().getPhoneNumber() : null,
                addMemberRequest.getPhone() != null ? addMemberRequest.getPhone().getCountryCode() : null);

        Optional<UnregisteredUser> unregisteredUser = unregisteredUserRepository.findByEmailOrPhone(
                addMemberRequest.getEmail(),
                addMemberRequest.getPhone() != null ? addMemberRequest.getPhone().getPhoneNumber() : null,
                addMemberRequest.getPhone() != null ? addMemberRequest.getPhone().getCountryCode() : null
        );

        // If the user is already registered, add them to a group as a registered member
        // If the user is unregistered, add them to a group as an unregistered member
        User registeredMember = null;
        UnregisteredUser unregisteredMember = null;
        if (user.isPresent()) {
            registeredMember = user.get();
        } else if (unregisteredUser.isPresent()) {
            unregisteredMember = unregisteredUser.get();
        } else {
            UnregisteredUser newUnregisteredUser = new UnregisteredUser();
            if (addMemberRequest.getEmail() != null && !addMemberRequest.getEmail().trim().isBlank()) {
                newUnregisteredUser.setEmail(addMemberRequest.getEmail());
            } else {
                newUnregisteredUser.setCountryCode(addMemberRequest.getPhone().getCountryCode());
                newUnregisteredUser.setPhoneNumber(addMemberRequest.getPhone().getPhoneNumber());
            }
            unregisteredMember = unregisteredUserRepository.save(newUnregisteredUser);
        }
        GroupMembers groupMembers = new GroupMembers();
        groupMembers.setGroup(group);
        groupMembers.setMember(registeredMember);
        groupMembers.setUnregisteredMember(unregisteredMember);
        groupMembers.setName(addMemberRequest.getName());

        UUID memberId = groupMembersRepository.save(groupMembers).getId();

        return "User - " + memberId + " successfully added as member of the group.";
    }

    // This method checks if either email or phone number is provided in the request to add a member.
    private void checkForContactOrEmail(GroupMemberDTO addMemberRequest) {
        boolean isEmailEmpty = addMemberRequest.getEmail() == null || addMemberRequest.getEmail().trim().isEmpty();
        boolean isPhoneEmpty = addMemberRequest.getPhone() == null
                || addMemberRequest.getPhone().getPhoneNumber() == 0
                || addMemberRequest.getPhone().getCountryCode() == null;
        if(isEmailEmpty && isPhoneEmpty)
            throw new BadRequestException("Either email or phone number must be provided to add a member!");
    }

    @Override
    @PreAuthorize("@authorizationService.isGroupOwner(#groupId)")
    public List<UserDTO> findMembers(UUID groupId) {

        // TODO : change this to also user unregistered user
        // TODO : change this to return user name and id if exits not member from group members table

        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DataNotFoundException(ErrorMessages.GROUP_NOT_FOUND));
        List<GroupMembers> groupMembersList = groupMembersRepository.findByGroupId(group.getId());
        List<UserDTO> userDTOS = new ArrayList<>();
        for (GroupMembers groupMembers : groupMembersList) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUserId(groupMembers.getMember().getId());
            userDTO.setEmail(groupMembers.getMember().getEmail());
            userDTO.setUsername(groupMembers.getMember().getUsername());
            userDTO.setPhone(new PhoneDTO(groupMembers.getMember().getCountryCode(), groupMembers.getMember().getPhoneNumber()));
            userDTOS.add(userDTO);
        }
        return userDTOS;
    }

    @Override
    @Transactional
    @PreAuthorize("@authorizationService.isGroupOwner(#groupId)")
    public String deleteMembers(UUID groupId, UUID groupMemberId) {
        groupMembersRepository.deleteById(groupMemberId);
        return "Member successfully removed from the group!";
    }

    @Override
    @Transactional
    @PreAuthorize("@authorizationService.isGroupOwner(#groupId)")
    public String deleteGroupByGroupId(UUID groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DataNotFoundException(ErrorMessages.GROUP_NOT_FOUND));

        if(!group.isDefaultGroup())
            groupRepository.deleteById(groupId);
        else
            throw new AccessDeniedException("This group is default so can't be delete");

        return "Successfully deleted the group - %s.".formatted(groupId);
    }

    @Override
    @Transactional
    @PreAuthorize("@authorizationService.isGroupOwner(#groupId)")
    public GroupDTO findGroupByGroupId(UUID groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DataNotFoundException(ErrorMessages.GROUP_NOT_FOUND));
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setGroupId(group.getId());
        groupDTO.setGroupName(group.getGroupName());
        groupDTO.setOwner(group.getUser().getUsername());

        List<Expense> expenseList = expenseRepository.findByGroupId(groupId);
        List<ExpenseListDTO> expenseDTOList = getExpenseListDTOS(expenseList);
        groupDTO.setExpenses(expenseDTOList);

        List<GroupMemberDTO> groupMemberDTOS = getGroupMemberDTOS(group);
        groupDTO.setGroupMembers(groupMemberDTOS);

        return groupDTO;
    }

    private static List<ExpenseListDTO> getExpenseListDTOS(List<Expense> expenseList) {
        List<ExpenseListDTO> expenseDTOList = new ArrayList<>();
        for (Expense expense : expenseList) {
            ExpenseListDTO expenseListDTO = new ExpenseListDTO();
            expenseListDTO.setExpenseId(expense.getId());
            expenseListDTO.setAmount(expense.getAmount());
            expenseListDTO.setDescription(expense.getDescription());
            expenseListDTO.setExpenseCreatedAt(String.valueOf(expense.getCreatedOn()));
            expenseDTOList.add(expenseListDTO);
        }
        return expenseDTOList;
    }

    private static List<GroupMemberDTO> getGroupMemberDTOS(Group group) {
        List<GroupMembers> groupMembers = group.getGroupMembers();
        List<GroupMemberDTO> groupMemberDTOS = new ArrayList<>();
        for (GroupMembers groupMember : groupMembers) {
            User registeredUser = groupMember.getMember();
            UnregisteredUser unregisteredUser = groupMember.getUnregisteredMember();

            GroupMemberDTO groupMemberDTO = new GroupMemberDTO();
            groupMemberDTO.setGroupMemberId(groupMember.getId());
            groupMemberDTO.setName(groupMember.getName());
            if (registeredUser != null) {
                // If the member is registered user
                groupMemberDTO.setEmail(registeredUser.getEmail());
                groupMemberDTO.setPhone(new PhoneDTO(registeredUser.getCountryCode(), registeredUser.getPhoneNumber()));
                groupMemberDTO.setUsername(registeredUser.getUsername());
            } else {
                // If member is unregistered user
                groupMemberDTO.setEmail(unregisteredUser.getEmail());
                groupMemberDTO.setPhone(new PhoneDTO(unregisteredUser.getCountryCode(), unregisteredUser.getPhoneNumber()));
            }
            groupMemberDTOS.add(groupMemberDTO);
        }
        return groupMemberDTOS;
    }

    @Override
    @Transactional
    @PreAuthorize("@authorizationService.isGroupOwner(#groupId)")
    public String updateGroup(GroupUpdateRequest groupUpdateRequest, UUID groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DataNotFoundException(ErrorMessages.GROUP_NOT_FOUND));
        group.setGroupName(groupUpdateRequest.getGroupName());
        groupRepository.save(group);
        return "%s - Group update successfully!".formatted(group.getId());
    }

    @Override
    @Transactional
    @PreAuthorize("@authorizationService.isGroupOwner(#groupId)")
    public BalanceTransaction getBalanceTransactions(UUID groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DataNotFoundException(ErrorMessages.GROUP_NOT_FOUND));
        // Fetch all the transactions related to the group
        List<Transaction<UUID>> transactions = expenseRepository.findTransactionsByGroupId(group.getId());
        SettlementStrategy<UUID> settlementStrategy = new SortSettlementStrategy<>();
        List<Transaction<UUID>> balancingTransactions = settlementStrategy.settle(transactions);

        List<UserTransaction> userTransactions = balancingTransactions.stream().map(transaction -> {
            GroupMembers debtor = groupMembersRepository.findById(transaction.getDebtor()).orElseThrow(() -> new DataNotFoundException("Debtor not found in group members"));
            GroupMembers creditor = groupMembersRepository.findById(transaction.getCreditor()).orElseThrow(() -> new DataNotFoundException("Creditor not found in group members"));
            String debtorName = debtor.getName();
            String creditorName = creditor.getName();

            UserTransaction userTransaction = new UserTransaction();
            userTransaction.setDebtorId(transaction.getDebtor());
            userTransaction.setDebtorName(debtorName);
            userTransaction.setCreditorId(transaction.getCreditor());
            userTransaction.setCreditorName(creditorName);
            userTransaction.setAmount(transaction.getAmount());

            return userTransaction;
        }).toList();

        BalanceTransaction balanceTransaction = new BalanceTransaction();
        balanceTransaction.setGroupId(group.getId());
        balanceTransaction.setGroupName(group.getGroupName());
        balanceTransaction.setTransactions(userTransactions);

        return balanceTransaction;
    }
}
