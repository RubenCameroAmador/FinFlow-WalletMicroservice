package com.rubencamero.finflow.infrastructure.api;

import com.rubencamero.finflow.application.command.*;
import com.rubencamero.finflow.application.usecase.*;
import com.rubencamero.finflow.domain.valueobject.WalletId;
import com.rubencamero.finflow.infrastructure.api.dto.*;
import com.rubencamero.finflow.infrastructure.api.mapper.WalletApiMapper;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final CreateWalletUseCase createWalletUseCase;
    private final GetWalletUseCase getWalletUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final FreezeWalletUseCase freezeWalletUseCase;
    private final ActivateWalletUseCase activateWalletUseCase;

    public WalletController(CreateWalletUseCase createWalletUseCase,
                            GetWalletUseCase getWalletUseCase,
                            DepositMoneyUseCase depositMoneyUseCase,
                            WithdrawMoneyUseCase withdrawMoneyUseCase,
                            FreezeWalletUseCase freezeWalletUseCase,
                            ActivateWalletUseCase activateWalletUseCase) {
        this.createWalletUseCase = createWalletUseCase;
        this.getWalletUseCase = getWalletUseCase;
        this.depositMoneyUseCase = depositMoneyUseCase;
        this.withdrawMoneyUseCase = withdrawMoneyUseCase;
        this.freezeWalletUseCase = freezeWalletUseCase;
        this.activateWalletUseCase = activateWalletUseCase;
    }

    @PostMapping
    public WalletResponse createWallet(@RequestBody CreateWalletRequest request) {
        CreateWalletCommand command = WalletApiMapper.toCommand(request);
        var wallet = createWalletUseCase.execute(command);
        return WalletApiMapper.toResponse(wallet);
    }

    @GetMapping("/{walletId}")
    public WalletResponse getWallet(@PathVariable String walletId) {
        GetWalletQuery query = new GetWalletQuery(WalletId.of(UUID.fromString(walletId)));
        var wallet = getWalletUseCase.execute(query);
        return WalletApiMapper.toResponse(wallet);
    }

    @PostMapping("/{walletId}/deposit")
    public MessageResponse depositMoney(@PathVariable String walletId,
                                        @RequestBody DepositRequest body) {
        WalletId id = WalletId.of(UUID.fromString(walletId));
        DepositMoneyCommand command = WalletApiMapper.toCommand(body, id);
        depositMoneyUseCase.execute(command);
        return new MessageResponse("Deposit successful");
    }

    @PostMapping("/{walletId}/withdraw")
    public MessageResponse withdrawMoney(@PathVariable String walletId,
                                         @RequestBody WithdrawRequest body) {
        WalletId id = WalletId.of(UUID.fromString(walletId));
        WithdrawMoneyCommand command = WalletApiMapper.toCommand(body, id);
        withdrawMoneyUseCase.execute(command);
        return new MessageResponse("Withdrawal successful");
    }

    @PutMapping("/{walletId}/freeze")
    public MessageResponse freezeWallet(@PathVariable String walletId) {
        WalletId id = WalletId.of(UUID.fromString(walletId));
        FreezeWalletCommand command = new FreezeWalletCommand(id);
        freezeWalletUseCase.execute(command);
        return new MessageResponse("Wallet frozen successfully");
    }

    @PutMapping("/{walletId}/activate")
    public MessageResponse activateWallet(@PathVariable String walletId) {
        WalletId id = WalletId.of(UUID.fromString(walletId));
        ActivateWalletCommand command = new ActivateWalletCommand(id);
        activateWalletUseCase.execute(command);
        return new MessageResponse("Wallet activated successfully");
    }
}
