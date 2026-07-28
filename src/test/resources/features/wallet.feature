Feature: Wallet management
  As a wallet owner
  I want to create a wallet, deposit and withdraw money, and freeze/activate it
  So that I can manage my funds safely

  Scenario: Creating a new wallet
    Given an owner wants to create a wallet with an initial balance of 100.00 USD
    When the wallet is created
    Then the wallet should be active
    And the wallet balance should be 100.00 USD

  Scenario: Depositing money into an active wallet
    Given an active wallet with a balance of 100.00 USD
    When 50.00 USD is deposited into the wallet
    Then the wallet balance should be 150.00 USD

  Scenario: Withdrawing money from an active wallet
    Given an active wallet with a balance of 100.00 USD
    When 40.00 USD is withdrawn from the wallet
    Then the wallet balance should be 60.00 USD

  Scenario: Withdrawing more money than the available balance
    Given an active wallet with a balance of 30.00 USD
    When 50.00 USD is withdrawn from the wallet
    Then the operation should be rejected with message "Insufficient balance."

  Scenario: Depositing money into a frozen wallet
    Given a frozen wallet with a balance of 100.00 USD
    When 10.00 USD is deposited into the wallet
    Then the operation should be rejected with message "Wallet is Frozen, cannot deposit money"

  Scenario: Freezing an active wallet
    Given an active wallet with a balance of 100.00 USD
    When the wallet is frozen
    Then the wallet should be frozen

  Scenario: Activating a frozen wallet
    Given a frozen wallet with a balance of 100.00 USD
    When the wallet is activated
    Then the wallet should be active
