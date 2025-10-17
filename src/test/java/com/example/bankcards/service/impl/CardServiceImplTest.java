package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardStatus;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardNumberTakenException;
import com.example.bankcards.exception.ImpossibleMoneyTransferException;
import com.example.bankcards.exception.InvalidCardNumberException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.NumberEncryptionUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private static User user;
    private static Card card;
    private static Card justCreatedCard;
    private static final Long CARD_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final String CARD_NUM = "1111222233334444";
    private static final String TRUNCATED_CARD_NUM = "**** **** **** 4444";
    private static final String INVALID_CARD_NUM = "111A222233334444";
    private static final String SECOND_CARD_NUM = "5555666677778888";

    @BeforeAll
    static void setUp() {
        user = User.builder()
                .id(USER_ID)
                .fullName("Test User")
                .username("test_user")
                .password("pass")
                .build();

        card = Card.builder()
                .id(CARD_ID)
                .cardNum(CARD_NUM)
                .owner(user.getFullName())
                .expirationDate(LocalDateTime.now().plusYears(10L))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .user(user)
                .build();

        justCreatedCard = Card.builder()
                .id(CARD_ID)
                .cardNum(CARD_NUM)
                .owner(user.getFullName())
                .expirationDate(LocalDateTime.now().plusYears(10L))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();
    }

    @BeforeEach
    void resetCardBalance() {
        card.setBalance(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("createCard: Успешное создание карты")
    void createCard_Success() {
        CreateCardRequest request = new CreateCardRequest(CARD_NUM, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNum(CARD_NUM)).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenReturn(justCreatedCard);

        CardDto result = cardService.createCard(request);

        assertNotNull(result);
        assertEquals(NumberEncryptionUtil.encryptCardNumber(CARD_NUM), result.getCardNum());
        assertEquals(CardStatus.ACTIVE.name(), result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getBalance());

        verify(userRepository, times(1)).findById(USER_ID);
        verify(cardRepository, times(1)).findByCardNum(CARD_NUM);
        verify(cardRepository, times(1)).save(any(Card.class));

    }

    @Test
    @DisplayName("createCard: Ошибка - Невалидный номер карты (содержит буквы)")
    void createCard_Failure_InvalidCardNumber() {
        CreateCardRequest request = new CreateCardRequest(INVALID_CARD_NUM, USER_ID);

        Exception exception = assertThrows(InvalidCardNumberException.class, () -> {
            cardService.createCard(request);
        });

        assertEquals("The card number contains characters that are not numbers!", exception.getMessage());
        verify(userRepository, never()).findById(any());
        verify(cardRepository, never()).findByCardNum(any());
    }

    @Test
    @DisplayName("createCard: Ошибка - Пользователь не найден")
    void createCard_Failure_UserNotFound() {
        CreateCardRequest request = new CreateCardRequest(CARD_NUM, 99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            cardService.createCard(request);
        });

        assertEquals("User with id <<99>> not found!", exception.getMessage());
        verify(cardRepository, never()).findByCardNum(any());
    }

    @Test
    @DisplayName("createCard: Ошибка - Номер карты уже занят")
    void createCard_Failure_CardNumberTaken() {
        CreateCardRequest request = new CreateCardRequest(CARD_NUM, USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNum(CARD_NUM)).thenReturn(Optional.of(card));

        Exception exception = assertThrows(CardNumberTakenException.class, () -> {
            cardService.createCard(request);
        });

        assertEquals("Card with number <<" + CARD_NUM + ">> already exists!", exception.getMessage());
    }

    @Test
    @DisplayName("deleteCard: Успешное удаление карты")
    void deleteCard_Success() {
        doNothing().when(cardRepository).deleteById(CARD_ID);

        cardService.deleteCard(CARD_ID);

        verify(cardRepository, times(1)).deleteById(CARD_ID);
    }

    @Test
    @DisplayName("changeStatus: Успешная смена статуса карты")
    void changeStatus_Success() {
        Card cardBeforeUpdate = Card.builder()
                .id(CARD_ID)
                .cardNum(CARD_NUM)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(cardBeforeUpdate));

        CardDto result = cardService.changeStatus(CARD_ID, CardStatus.BLOCKED);

        assertNotNull(result);
        assertEquals(CardStatus.BLOCKED.name(), result.getStatus());
        verify(cardRepository, times(1)).findById(CARD_ID);
    }

    @Test
    @DisplayName("changeStatus: Ошибка - Карта не найдена")
    void changeStatus_Failure_CardNotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            cardService.changeStatus(99L, CardStatus.BLOCKED);
        });

        assertEquals("Card with id <<99>> not found!", exception.getMessage());
    }

    @Test
    @DisplayName("findById: Успешный поиск карты по ID")
    void findById_Success() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        CardDto result = cardService.findById(CARD_ID);

        assertNotNull(result);
        assertEquals(TRUNCATED_CARD_NUM, result.getCardNum());
        verify(cardRepository, times(1)).findById(CARD_ID);
    }

    @Test
    @DisplayName("findById: Ошибка - Карта не найдена")
    void findById_Failure_CardNotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CardNotFoundException.class, () -> {
            cardService.findById(99L);
        });

        assertEquals("Card with id <<99>> not found!", exception.getMessage());
    }


    @Test
    @DisplayName("findAllByUserId: Успешное получение страницы карт пользователя")
    void findAllByUserId_Success() {
        Page<Card> cardPage = new PageImpl<>(List.of(card));

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(cardPage);

        Page<CardDto> result = cardService.findAllByUserId(USER_ID, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(NumberEncryptionUtil.encryptCardNumber(CARD_NUM), result.getContent().get(0).getCardNum());

        verify(cardRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("findAll: Успешное получение страницы всех карт")
    void findAll_Success() {
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        Pageable pageable = PageRequest.of(0, 10);

        when(cardRepository.findAll(eq(pageable))).thenReturn(cardPage);

        Page<CardDto> result = cardService.findAll(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(NumberEncryptionUtil.encryptCardNumber(CARD_NUM), result.getContent().get(0).getCardNum());

        verify(cardRepository, times(1)).findAll(eq(pageable));
    }

    @Test
    @DisplayName("transfer: Успешный перевод средств")
    void transfer_Success() {
        Card senderCard = Card.builder()
                .id(1L)
                .cardNum(CARD_NUM)
                .user(user)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .build();
        Card recipientCard = Card.builder()
                .id(2L)
                .cardNum(SECOND_CARD_NUM)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("500.00"))
                .build();
        BigDecimal amount = new BigDecimal("100.00");

        when(cardRepository.findAllByUserId(USER_ID)).thenReturn(List.of(senderCard));
        when(cardRepository.findByCardNum(CARD_NUM)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNum(SECOND_CARD_NUM)).thenReturn(Optional.of(recipientCard));

        cardService.transfer(USER_ID, CARD_NUM, SECOND_CARD_NUM, amount);

        assertEquals(new BigDecimal("900.00"), senderCard.getBalance());
        assertEquals(new BigDecimal("600.00"), recipientCard.getBalance());
        verify(cardRepository, times(1)).findAllByUserId(USER_ID);
        verify(cardRepository, times(1)).findByCardNum(CARD_NUM);
        verify(cardRepository, times(1)).findByCardNum(SECOND_CARD_NUM);
    }

    @Test
    @DisplayName("transfer: Ошибка - Карта отправителя не принадлежит пользователю")
    void transfer_Failure_SenderCardNotBelongToUser() {
        Card otherCard = Card.builder().id(2L).cardNum(SECOND_CARD_NUM).user(user).status(CardStatus.ACTIVE).balance(new BigDecimal("1000.00")).build();
        when(cardRepository.findAllByUserId(USER_ID)).thenReturn(List.of(otherCard));

        Exception exception = assertThrows(ImpossibleMoneyTransferException.class, () -> {
            cardService.transfer(USER_ID, CARD_NUM, SECOND_CARD_NUM, new BigDecimal("100.00"));
        });

        assertEquals("Card with number <<" + CARD_NUM + ">> doesn't belong to this user!", exception.getMessage());
    }

    @Test
    @DisplayName("transfer: Ошибка - Невалидный номер карты")
    void transfer_Failure_InvalidCardNumberTransfer() {
        Exception exception = assertThrows(InvalidCardNumberException.class, () -> {
            cardService.transfer(USER_ID, INVALID_CARD_NUM, SECOND_CARD_NUM, new BigDecimal("100.00"));
        });

        assertEquals("The card number contains characters that are not numbers!", exception.getMessage());
    }

    @Test
    @DisplayName("transfer: Ошибка - Недостаточно средств")
    void transfer_Failure_InsufficientFunds() {
        Card senderCard = Card.builder()
                .id(1L).cardNum(CARD_NUM)
                .user(user)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("50.00"))
                .build();

        Card recipientCard = Card.builder()
                .id(2L)
                .cardNum(SECOND_CARD_NUM)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("500.00"))
                .build();
        BigDecimal amount = new BigDecimal("100.00");

        when(cardRepository.findAllByUserId(USER_ID)).thenReturn(List.of(senderCard));
        when(cardRepository.findByCardNum(CARD_NUM)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNum(SECOND_CARD_NUM)).thenReturn(Optional.of(recipientCard));

        Exception exception = assertThrows(ImpossibleMoneyTransferException.class, () -> {
            cardService.transfer(USER_ID, CARD_NUM, SECOND_CARD_NUM, amount);
        });

        assertEquals("Insufficient funds on the sender's card.", exception.getMessage());
    }

    @Test
    @DisplayName("transfer: Ошибка - Карта-получатель заблокирована")
    void transfer_Failure_RecipientCardBlocked() {
        Card senderCard = Card.builder()
                .id(1L)
                .cardNum(CARD_NUM)
                .user(user)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .build();
        Card recipientCard = Card.builder().
                id(2L)
                .cardNum(SECOND_CARD_NUM)
                .status(CardStatus.BLOCKED)
                .balance(new BigDecimal("500.00"))
                .build();
        BigDecimal amount = new BigDecimal("100.00");

        when(cardRepository.findAllByUserId(USER_ID)).thenReturn(List.of(senderCard));
        when(cardRepository.findByCardNum(CARD_NUM)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNum(SECOND_CARD_NUM)).thenReturn(Optional.of(recipientCard));

        Exception exception = assertThrows(ImpossibleMoneyTransferException.class, () -> {
            cardService.transfer(USER_ID, CARD_NUM, SECOND_CARD_NUM, amount);
        });

        assertEquals("One of the cards is blocked or expired.", exception.getMessage());
    }

    @Test
    @DisplayName("depositMoney: Успешное пополнение карты")
    void depositMoney_Success() {
        Card depositCard = Card.builder()
                .id(CARD_ID)
                .cardNum(CARD_NUM)
                .user(user)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .build();
        BigDecimal amount = new BigDecimal("50.00");

        when(cardRepository.findAllByUserId(USER_ID)).thenReturn(List.of(depositCard));
        when(cardRepository.findByCardNum(CARD_NUM)).thenReturn(Optional.of(depositCard));

        cardService.depositMoney(USER_ID, CARD_NUM, amount);

        assertEquals(new BigDecimal("150.00"), depositCard.getBalance());
        verify(cardRepository, times(1)).findAllByUserId(USER_ID);
        verify(cardRepository, times(1)).findByCardNum(CARD_NUM);
    }

    @Test
    @DisplayName("depositMoney: Ошибка - Карта заблокирована/просрочена")
    void depositMoney_Failure_CardBlocked() {
        Card blockedCard = Card.builder()
                .id(CARD_ID)
                .cardNum(CARD_NUM)
                .user(user)
                .status(CardStatus.BLOCKED)
                .balance(new BigDecimal("100.00"))
                .build();
        BigDecimal amount = new BigDecimal("50.00");

        when(cardRepository.findAllByUserId(USER_ID)).thenReturn(List.of(blockedCard));
        when(cardRepository.findByCardNum(CARD_NUM)).thenReturn(Optional.of(blockedCard));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cardService.depositMoney(USER_ID, CARD_NUM, amount);
        });

        assertEquals("You cannot deposit money onto this card: This card is blocked or expired.", exception.getMessage());
    }

    @Test
    @DisplayName("depositMoney: Ошибка - Сумма пополнения меньше или равна нулю")
    void depositMoney_Failure_InvalidAmount() {
        when(cardRepository.findAllByUserId(USER_ID)).thenReturn(List.of(card));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cardService.depositMoney(USER_ID, CARD_NUM, BigDecimal.ZERO);
        });

        assertEquals("Transfer amount must be greater than zero.", exception.getMessage());
    }


    @Test
    @DisplayName("blockCard: Успешная блокировка карты")
    void blockCard_Success() {
        Card activeCard = Card.builder()
                .id(CARD_ID)
                .cardNum(CARD_NUM)
                .user(user).status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO).build();

        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(activeCard));

        cardService.blockCard(USER_ID, CARD_ID);

        assertEquals(CardStatus.BLOCKED, activeCard.getStatus());
        verify(cardRepository, times(1)).findById(CARD_ID);
    }

    @Test
    @DisplayName("blockCard: Ошибка - Попытка блокировки чужой карты")
    void blockCard_Failure_NotOwner() {
        User otherUser = User.builder()
                .id(2L)
                .build();

        Card activeCard = Card.builder()
                .id(CARD_ID)
                .cardNum(CARD_NUM)
                .user(otherUser)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(activeCard));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cardService.blockCard(USER_ID, CARD_ID);
        });

        assertEquals("Card with id <<" + CARD_ID + ">> doesn't belong to this user!", exception.getMessage());
    }

    @Test
    @DisplayName("blockCard: Ошибка - Карта уже заблокирована")
    void blockCard_Failure_AlreadyBlocked() {
        Card blockedCard = Card.builder()
                .id(CARD_ID)
                .cardNum(CARD_NUM)
                .user(user)
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.ZERO)
                .build();

        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(blockedCard));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cardService.blockCard(USER_ID, CARD_ID);
        });

        assertEquals("This card has already been blocked or has expired.", exception.getMessage());
    }

    @Test
    @DisplayName("getBalance: Успешное получение баланса")
    void getBalance_Success() {
        Card cardWithBalance = Card.builder()
                .id(CARD_ID)
                .cardNum(CARD_NUM)
                .user(user)
                .balance(new BigDecimal("123.45"))
                .build();

        when(cardRepository.findByCardNumAndUser_Id(CARD_NUM, USER_ID)).thenReturn(Optional.of(cardWithBalance));

        BigDecimal balance = cardService.getBalance(USER_ID, CARD_NUM);

        assertEquals(new BigDecimal("123.45"), balance);
        verify(cardRepository, times(1)).findByCardNumAndUser_Id(CARD_NUM, USER_ID);
    }

    @Test
    @DisplayName("getBalance: Ошибка - Невалидный номер карты")
    void getBalance_Failure_InvalidCardNum() {
        Exception exception = assertThrows(InvalidCardNumberException.class, () -> {
            cardService.getBalance(USER_ID, INVALID_CARD_NUM);
        });

        assertEquals("The card number contains characters that are not numbers!", exception.getMessage());
    }

    @Test
    @DisplayName("getBalance: Ошибка - Карта не найдена или не принадлежит пользователю")
    void getBalance_Failure_CardNotFoundOrNotOwned() {
        when(cardRepository.findByCardNumAndUser_Id(CARD_NUM, USER_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CardNotFoundException.class, () -> {
            cardService.getBalance(USER_ID, CARD_NUM);
        });

        assertEquals("Card with cardNum <<" + CARD_NUM
                + ">> doesn't belong to this user or not found!", exception.getMessage());
    }

}