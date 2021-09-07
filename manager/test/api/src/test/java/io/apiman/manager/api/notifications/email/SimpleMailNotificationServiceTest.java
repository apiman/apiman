package io.apiman.manager.api.notifications.email;

import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class SimpleMailNotificationServiceTest {

    @Test
    public void test() {
        PatriciaTrie<EmailNotificationTemplate> trie = new PatriciaTrie<>();
        trie.put("apiman", new EmailNotificationTemplate().setNotificationTemplateBody("x"));
        trie.put("apiman.foo.bar", new EmailNotificationTemplate().setNotificationTemplateBody("x"));
        trie.put("apiman.foo", new EmailNotificationTemplate().setNotificationTemplateBody("y"));

        //System.out.println(trie.select("apiman.foo.bar.x"));
        System.out.println(trie.headMap("apiman.foo.bar*").size());
    }
}
