package com.patricia.subscriptionApp.service.impl;

import com.patricia.subscriptionApp.entity.Payment;
import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.dto.SubscriptionDto;
import com.patricia.subscriptionApp.enums.PlanType;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.mapper.SubscriptionMapper;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import com.patricia.subscriptionApp.exception.ResourceNotFoundException;
import com.patricia.subscriptionApp.exception.BadRequestException;
import com.patricia.subscriptionApp.entity.User;
import com.patricia.subscriptionApp.repository.UserRepository;
import com.patricia.subscriptionApp.service.PaymentService;
import com.patricia.subscriptionApp.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

/**
 * Service class for Subscription entity operations.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository repository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    @Override
    public Subscription suspendSubscription(Payment payment) {
        Subscription subscription = payment.getSubscription();
        subscription.setStatus(SubscriptionStatus.SUSPENSA);
        return repository.save(subscription);
    }

    @Override
    public List<Subscription> findAll() {
        log.debug("Finding all Subscription entities");
        return repository.findAll();
    }

    @Override
    public Page<Subscription> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        log.debug("Paginating Subscription — page={}, size={}, sort={} {}", page, size, sortBy, sortDirection);

        if (page < 0) throw new BadRequestException("Page number cannot be negative");
        if (size <= 0) throw new BadRequestException("Page size must be greater than 0");
        if (size > 100) {
            log.warn("Page size {} exceeds maximum, capping at 100", size);
            size = 100;
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Subscription> result = repository.findAll(pageable);

        log.debug("Found {} of {} total Subscription entities on page {}",
                result.getNumberOfElements(), result.getTotalElements(), result.getNumber());
        return result;
    }

    @Override
    public Subscription findById(UUID id) {
        log.debug("Finding Subscription by id={}", id);
        if (id == null) throw new BadRequestException("ID cannot be null");

        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
    }

    @Transactional
    @Override
    public Subscription create(SubscriptionDto dto) {
        log.info("Creating Subscription from dto={}", dto);
        if (dto == null) throw new BadRequestException("DTO cannot be null");

        if (dto.getUserId() == null) {
            throw new BadRequestException("UserId cannot be null");
        }

        User user = userRepository.findByIdForUpdate(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getUserId()));

        boolean existsActiveSub = repository.existsByUser_IdAndStatus(dto.getUserId(), SubscriptionStatus.ATIVA);

        if(existsActiveSub){
            throw new BadRequestException("A user can have only one subscription with an ATIVA status.");
        }

        Subscription subscription = SubscriptionMapper.toEntity(dto);
        subscription.setUser(user);
        subscription.setStatus(SubscriptionStatus.ATIVA);
        subscription.setDataInicio(LocalDate.now());
        subscription.setDataExpiracao(LocalDate.now().plusMonths(1));

        Subscription saved = repository.save(subscription);

        paymentService.createNewPaymentAndSendRequest(saved);
        log.info("Created Subscription id={}", saved.getId());
        return saved;
    }

    @Transactional
    @Override
    public Subscription cancel(UUID id) {
        log.info("Canceling Subscription id={}", id);
        if (id == null)  throw new BadRequestException("ID cannot be null");

        Subscription subscription = findById(id);

        if(subscription.getStatus()!= SubscriptionStatus.ATIVA){
            throw new BadRequestException("You cannot cancel a not active subscription");
        }

        LocalDate today = LocalDate.now();
        if (!subscription.getDataExpiracao().isAfter(today)) {
            subscription.setStatus(SubscriptionStatus.CANCELADA);
        } else {
            subscription.setStatus(SubscriptionStatus.CANCELADA_PENDENTE);
        }
        Subscription updated = repository.save(subscription);
        log.info("Canceled Subscription id={}", updated.getId());
        return updated;
    }

    public Optional<Subscription> findActiveByUserId(UUID userId) {
        log.debug("Finding Subscription by UserId={}", userId);
        if (userId == null) throw new BadRequestException("ID cannot be null");

        return repository.findByUser_IdAndStatusForUpdate(userId, SubscriptionStatus.ATIVA);
    }

    @Override
    public List<Subscription> findAllByUserId(UUID userId) {
        log.debug("Finding Subscription by UserId={}", userId);
        if (userId == null) throw new BadRequestException("User ID cannot be null");

        return repository.findByUser_Id(userId);
    }

    @Override
    public Optional<Subscription> findCurrentSubscription(UUID userId) {
        log.debug("Finding current Subscription by UserId={}", userId);
        if (userId == null) throw new BadRequestException("User ID cannot be null");

        return repository.findCurrentByUserIdAt(userId, LocalDate.now());
    }

    @Override
    @Transactional
    public Subscription updatePlanType(SubscriptionDto dto) {
        if (dto == null) {
            throw new BadRequestException("DTO cannot be null");
        }

        Subscription currentSubscription= null;

        if (dto.getId() == null && dto.getUserId() != null) {
            currentSubscription = findActiveByUserId(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription", "userId", dto.getUserId()));
        }else{
            currentSubscription =
                    repository.findByIdForUpdate(dto.getId())
                            .orElseThrow(()-> new ResourceNotFoundException("Subscription", "id", dto.getId()));
            if(!currentSubscription.getUser().getId().equals(dto.getUserId())){
                throw new BadRequestException("User ID in DTO does not match the subscription's user ID.");
            }

        }

        log.info("Updating Subscription id={} with dto={}", dto.getId(), dto);


        if(currentSubscription.getStatus() != SubscriptionStatus.ATIVA) {
            throw new BadRequestException("Only active subscriptions can be updated.");
        }

        PlanType newPlan = dto.getPlano();

        BigDecimal currentPlanValue = currentSubscription.getPlano().getValor();

        if (currentSubscription.getPlano().equals(newPlan)) {
            throw new BadRequestException("The plan type is the same as the current one.");
        }

        Optional<Subscription> preProcessed = repository.findByUser_IdAndStatusForUpdate(
                dto.getUserId(),
                SubscriptionStatus.PRE_PROCESSADA
        );

        if(preProcessed.isPresent()){
            throw new BadRequestException("User already has a pre-processed subscription. Cannot update plan type.");
        }

        if (currentPlanValue.compareTo(newPlan.getValor()) > 0) {
            return createPreProcessedSubscription(newPlan, currentSubscription);
        }

        Subscription currentSubscriptionSnapshot = Subscription.builder()
                .id(currentSubscription.getId())
                .user(currentSubscription.getUser())
                .plano(currentSubscription.getPlano())
                .dataInicio(currentSubscription.getDataInicio())
                .dataExpiracao(currentSubscription.getDataExpiracao())
                .status(currentSubscription.getStatus())
                .build();

        currentSubscription.setStatus(SubscriptionStatus.EXPIRADA);
        currentSubscription.setDataExpiracao(LocalDate.now());
        repository.save(currentSubscription);

        repository.flush();

        return createPartialSubscription(newPlan, currentSubscriptionSnapshot);
    }

    private Subscription createPreProcessedSubscription(PlanType newPlan, Subscription currentSubscription) {
        Subscription newSubscription = new Subscription();

        newSubscription.setUser(currentSubscription.getUser());
        newSubscription.setPlano(newPlan);
        newSubscription.setDataInicio(currentSubscription.getDataExpiracao());
        newSubscription.setDataExpiracao(currentSubscription.getDataExpiracao().plusMonths(1));
        newSubscription.setStatus(SubscriptionStatus.PRE_PROCESSADA);

        return repository.save(newSubscription);
    }

    @Transactional
    private Subscription createPartialSubscription(PlanType newPlan, Subscription currentSubscription) {

        Subscription newSubscription = new Subscription();

        newSubscription.setUser(currentSubscription.getUser());
        newSubscription.setPlano(newPlan);
        newSubscription.setDataInicio(LocalDate.now());
        newSubscription.setDataExpiracao(currentSubscription.getDataExpiracao());
        newSubscription.setStatus(SubscriptionStatus.ATIVA);

        Subscription saved = repository.save(newSubscription);

        paymentService.createPartialPaymentAndSendRequest(currentSubscription, saved);

        return saved;
    }

    public boolean existsById(UUID id) {
        return id != null && repository.existsById(id);
    }

    public long count() {
        return repository.count();
    }
}
