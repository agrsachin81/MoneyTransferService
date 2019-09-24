
package home.test.api.example.moneytransfer.mock;

import home.test.api.example.moneytransfer.spi.exceptions.AccountException;

public interface TransferServiceCallback<P> {

	P transfer(int index) throws AccountException;	
}