package com.invoice.service;

import com.invoice.dto.ClientRequest;
import com.invoice.dto.ClientResponse;
import com.invoice.entity.Client;
import com.invoice.entity.User;
import com.invoice.exception.DuplicateEmailException;
import com.invoice.exception.ResourceNotFoundException;
import com.invoice.repository.ClientRepository;
import com.invoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClientResponse create(ClientRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));

        // business rule — no duplicate emails per user
        // same email can exist for different users
        // but same user can't have two clients with same email
        if(clientRepository.existsByEmailAndUserId(request.getEmail(),
                user.getId()))
            throw new DuplicateEmailException(request.getEmail());

        Client client = Client.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .companyName(request.getCompanyName())
                .user(user)
                .build();

        Client saved = clientRepository.save(client);
        log.info("Client created: {} for user: {}", saved.getId(), userEmail);
        return mapToResponse(saved);
    }

    public List<ClientResponse> getAllByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));

        return clientRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClientResponse getById(Long id, String userEmail) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        // security check
        if(!client.getUser().getEmail().equals(userEmail))
            throw new ResourceNotFoundException("Client", id);

        return mapToResponse(client);
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request,
                                 String userEmail) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        if(!client.getUser().getEmail().equals(userEmail))
            throw new ResourceNotFoundException("Client", id);

        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());
        client.setCompanyName(request.getCompanyName());

        return mapToResponse(clientRepository.save(client));
    }

    @Transactional
    public void delete(Long id, String userEmail) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        if(!client.getUser().getEmail().equals(userEmail))
            throw new ResourceNotFoundException("Client", id);

        clientRepository.delete(client);
    }

    private ClientResponse mapToResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .address(client.getAddress())
                .companyName(client.getCompanyName())
                .build();
    }
}