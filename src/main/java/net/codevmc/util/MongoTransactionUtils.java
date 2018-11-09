package net.codevmc.util;

import com.mongodb.MongoException;
import com.mongodb.client.ClientSession;

import java.util.concurrent.Callable;

public class MongoTransactionUtils {

    public static <T> T transactWithRetry(Callable<T> transactional) throws Exception {
        while (true) {
            try {
                return transactional.call();
            } catch (MongoException ex) {
                if (!ex.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL)) throw ex;
            }
        }
    }

    public static void commitWithRetry(ClientSession session) {
        while (true) {
            try {
                session.commitTransaction();
                break;
            } catch (MongoException e) {
                if (!e.hasErrorLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL)) throw e;
            }
        }

    }
}
