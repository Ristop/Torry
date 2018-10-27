package ut.ee.torry.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientConfigurationTest {

    @Test
    public void testNumberOfBytesInPeerId() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();

        assertEquals(20, clientConfiguration.peerId().getBytes().length);
    }

}
