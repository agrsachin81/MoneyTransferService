

package home.test.api.example.moneytransfer.service.impl.mem;

import home.test.api.example.moneytransfer.service.internal.AccountServiceInternal;

public interface AccountServiceFactory {
	AccountServiceInternal createAccountServiceInternal();
}