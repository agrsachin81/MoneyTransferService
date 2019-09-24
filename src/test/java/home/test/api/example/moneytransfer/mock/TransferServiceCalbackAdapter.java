
package home.test.api.example.moneytransfer.mock;

import home.test.api.example.moneytransfer.spi.exceptions.AccountException;

public interface TransferServiceCalbackAdapter<P> {

	P transfer(int index, String cpAccountIdForIndex, String originatingAccount) throws AccountException;

}
