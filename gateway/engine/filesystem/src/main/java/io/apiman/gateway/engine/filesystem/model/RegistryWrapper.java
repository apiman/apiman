package io.apiman.gateway.engine.filesystem.model;

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pete Cornish
 */
public class RegistryWrapper {
    private List<Api> apis = new ArrayList<>();
    private List<Client> clients = new ArrayList<>();

    public List<Api> getApis() {
        return apis;
    }

    public void setApis(List<Api> apis) {
        this.apis = apis;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }
}
