package sangria.marshalling

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{BooleanNode, IntNode, NullNode, TextNode}
import org.scalatest.{Matchers, WordSpec}
import sangria.marshalling.jackson._
import sangria.marshalling.jackson.JacksonResultMarshaller
import sangria.marshalling.testkit._

class JacksonSupportSpec extends WordSpec with Matchers with MarshallingBehaviour with InputHandlingBehaviour {
  "Jackson integration" should {
    behave like `value (un)marshaller` (JacksonResultMarshaller)

    behave like `AST-based input unmarshaller` (jacksonFromInput[JsonNode])
    behave like `AST-based input marshaller` (JacksonResultMarshaller)

//    behave like `case class input unmarshaller`
//    behave like `case class input marshaller` (JacksonResultMarshaller)
  }


  val toRender = {

    val a = mapper.createArrayNode()
      .add(NullNode.instance)
      .add(IntNode.valueOf(123))
      .add(mapper.createArrayNode().add(mapper.createObjectNode().set("foo", TextNode.valueOf("bar"))))

    val b = mapper.createObjectNode()
    b.set("c", BooleanNode.valueOf(true))
    b.set("d", NullNode.instance)

    val obj = mapper.createObjectNode()
    obj.set("a", a)
    obj.set("b", b)
    obj
  }

  "InputUnmarshaller" should {
    "throw an exception on invalid scalar values" in {
      an [IllegalStateException] should be thrownBy
          JacksonInputUnmarshaller.getScalarValue(mapper.createObjectNode())
    }

    "throw an exception on variable names" in {
      an [IllegalArgumentException] should be thrownBy
          JacksonInputUnmarshaller.getVariableName(TextNode.valueOf("$foo"))
    }

    "render JSON values" in {
      val rendered = JacksonInputUnmarshaller.render(toRender)

      rendered should be ("""{"a":[null,123,[{"foo":"bar"}]],"b":{"c":true,"d":null}}""")
    }
  }

  "ResultMarshaller" should {
    "render pretty JSON values" in {
      val rendered = JacksonResultMarshaller.renderPretty(toRender)

      rendered.replaceAll("\r", "") should be (
        """{
          |  "a" : [
          |    null,
          |    123,
          |    [
          |      {
          |        "foo" : "bar"
          |      }
          |    ]
          |  ],
          |  "b" : {
          |    "c" : true,
          |    "d" : null
          |  }
          |}""".stripMargin.replaceAll("\r", ""))
    }

    "render compact JSON values" in {
      val rendered = JacksonResultMarshaller.renderCompact(toRender)

      rendered should be ("""{"a":[null,123,[{"foo":"bar"}]],"b":{"c":true,"d":null}}""")
    }
  }
}
