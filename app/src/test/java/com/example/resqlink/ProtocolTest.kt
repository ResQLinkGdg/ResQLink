import org.junit.Test
import com.example.resqlink.platform.reach.protocol.*

class ProtocolTest {

    @Test
    fun encodeDecode_sos_message() {
        val codec = MessageCodec()

        val msg = MessageFactory.newSos(
            senderId = "test-device",
            ttl = 5,
            text = "테스트 SOS"
        )

        val bytes = codec.encode(msg)
        val decoded = codec.decode(bytes)

        println("original=$msg")
        println("decoded=$decoded")
    }
}
