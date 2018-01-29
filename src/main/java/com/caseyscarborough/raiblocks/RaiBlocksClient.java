package com.caseyscarborough.raiblocks;

import com.caseyscarborough.raiblocks.exception.RaiBlocksException;
import com.caseyscarborough.raiblocks.request.AccountBalanceRequest;
import com.caseyscarborough.raiblocks.request.AccountBlockCountRequest;
import com.caseyscarborough.raiblocks.request.AccountCreateRequest;
import com.caseyscarborough.raiblocks.request.AccountInformationRequest;
import com.caseyscarborough.raiblocks.request.AccountPublicKeyRequest;
import com.caseyscarborough.raiblocks.response.AccountBalanceResponse;
import com.caseyscarborough.raiblocks.response.AccountBlockCountResponse;
import com.caseyscarborough.raiblocks.response.AccountCreateResponse;
import com.caseyscarborough.raiblocks.response.AccountInformationResponse;
import com.caseyscarborough.raiblocks.response.AccountPublicKeyResponse;
import com.caseyscarborough.raiblocks.response.BaseResponse;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class RaiBlocksClient {

    private final OkHttpClient client = new OkHttpClient();
    private final String host;
    private final Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();

    public RaiBlocksClient() {
        this("http://localhost:7076");
    }

    public RaiBlocksClient(String host) {
        this.host = host;
    }

    /**
     * Returns how many RAW is owned and how many have not yet been received for an account.
     *
     * @param account the address of the account
     */
    public AccountBalanceResponse getAccountBalance(String account) {
        AccountBalanceRequest request = new AccountBalanceRequest(account);
        return response(request, AccountBalanceResponse.class);
    }

    /**
     * Get number of blocks for a specific account.
     *
     * @param account the address of the account
     */
    public AccountBlockCountResponse getAccountBlockCount(String account) {
        AccountBlockCountRequest request = new AccountBlockCountRequest(account);
        return response(request, AccountBlockCountResponse.class);
    }

    /**
     * Returns frontier, open block, change representative block, balance, last
     * modified timestamp from local database& block count for account.
     *
     * @param account the address of the account
     * @see RaiBlocksClient#getAccountInformation(String, boolean, boolean, boolean)
     */
    public AccountInformationResponse getAccountInformation(String account) {
        return getAccountInformation(account, false, false, false);
    }

    /**
     * Returns frontier, open block, change representative block, balance, last
     * modified timestamp from local database & block count for account.
     * <p>
     * This overloaded version can additionally returns representative, voting
     * weight, and pending balance for account.
     *
     * @param account        the address of the account
     * @param representative whether or not to return the representative for the account
     * @param weight         whether or not to return the voting weight of the account
     * @param pending        whether or not to return the pending balance of the account
     * @see RaiBlocksClient#getAccountInformation(String)
     */
    public AccountInformationResponse getAccountInformation(String account,
                                                            boolean representative,
                                                            boolean weight,
                                                            boolean pending) {
        AccountInformationRequest request = new AccountInformationRequest(account, representative, weight, pending);
        return response(request, AccountInformationResponse.class);
    }

    /**
     * Creates a new account and inserts the next deterministic key in wallet.
     *
     * @param wallet the specified wallet.
     * @return the address of the new account.
     */
    public AccountCreateResponse createAccount(String wallet) {
        return createAccount(wallet, true);
    }

    /**
     * Creates a new account and inserts the next deterministic key in wallet.
     *
     * This overloaded version takes can disable the generating the work after
     * the account is created.
     *
     * @param wallet the specified wallet.
     * @param work whether or not generate work.
     * @return the address of the new account.
     */
    public AccountCreateResponse createAccount(String wallet, boolean work) {
        AccountCreateRequest request = new AccountCreateRequest(wallet, work);
        return response(request, AccountCreateResponse.class);
    }

    /**
     * Get the public key for an account.
     *
     * @param account the account to retrieve the public key for.
     * @return the account's public key.
     */
    public AccountPublicKeyResponse getAccountPublicKey(String account) {
        AccountPublicKeyRequest request = new AccountPublicKeyRequest(account);
        return response(request, AccountPublicKeyResponse.class);
    }

    private <T extends BaseResponse> T response(Object o, Class<T> clazz) {
        try {
            String json = gson.toJson(o);
            Request request = new Request.Builder()
                .post(RequestBody.create(MediaType.parse("application/json"), json))
                .url(host)
                .build();

            Response response = client.newCall(request).execute();
            String body = response.body().string();
            T t = gson.fromJson(body, clazz);
            if (!t.isSuccess()) {
                throw new RaiBlocksException(t.getError());
            }
            return t;
        } catch (IOException e) {
            throw new RaiBlocksException("Unable to get account balance", e);
        }
    }
}