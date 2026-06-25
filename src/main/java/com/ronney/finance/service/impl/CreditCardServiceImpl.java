package com.ronney.finance.service.impl;

import com.ronney.finance.dto.request.CreditCardRequest;
import com.ronney.finance.dto.response.CreditCardResponse;
import com.ronney.finance.repository.CreditCardRepository;
import com.ronney.finance.service.CreditCardService;
import com.ronney.finance.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditCardServiceImpl implements CreditCardService {
    private final CreditCardRepository creditCardRepository;
    private final CurrentUserService currentUserService;

    @Override
    public CreditCardResponse create(CreditCardRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<CreditCardResponse> findAll() {
        throw new UnsupportedOperationException(
                "Not implemented yet"
        );
    }

    @Override
    public CreditCardResponse findById(
            UUID id
    ) {
        throw new UnsupportedOperationException(
                "Not implemented yet"
        );
    }

    @Override
    public CreditCardResponse update(
            UUID id,
            CreditCardRequest request
    ) {
        throw new UnsupportedOperationException(
                "Not implemented yet"
        );
    }

    @Override
    public void delete(
            UUID id
    ) {
        throw new UnsupportedOperationException(
                "Not implemented yet"
        );
    }
}
