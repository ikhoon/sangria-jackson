package sangria.marshalling
import com.fasterxml.jackson.core.PrettyPrinter
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.databind.node.{BooleanNode, _}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter

import scala.collection.JavaConverters._


/**
  * Created by ikhoon on 15/02/2018.
  */
object jackson {
  val mapper = {
    val _mapper = new ObjectMapper() with ScalaObjectMapper
    _mapper.registerModule(DefaultScalaModule)
    val printer = new DefaultPrettyPrinter().withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
    _mapper.setDefaultPrettyPrinter(printer)
    _mapper
  }
  implicit object JacksonResultMarshaller extends ResultMarshaller {
    type Node = JsonNode
    type MapBuilder = ArrayMapBuilder[Node]


    def emptyMapNode(keys: Seq[String]): ArrayMapBuilder[JsonNode] = new ArrayMapBuilder[JsonNode](keys)

    def addMapNodeElem(builder: ArrayMapBuilder[JsonNode], key: String, value: JsonNode, optional: Boolean): ArrayMapBuilder[JsonNode] = builder.add(key, value)

    def mapNode(builder: ArrayMapBuilder[JsonNode]): JsonNode = {
      val on = mapper.createObjectNode()
      on.setAll(builder.toMap.asJava)
      on
    }

    def mapNode(keyValues: Seq[(String, JsonNode)]): JsonNode = {
      val on = mapper.createObjectNode()
      on.setAll(keyValues.toMap.asJava)
      on
    }

    def arrayNode(values: Vector[JsonNode]): JsonNode = {
      val an = mapper.createArrayNode()
      an.addAll(values.asJava)
    }

    def optionalArrayNodeValue(value: Option[JsonNode]): JsonNode = {
      value match {
        case Some(v) => v
        case None => nullNode
      }
    }

    def scalarNode(value: Any, typeName: String, info: Set[ScalarValueInfo]): JsonNode = value match {
      case v: String ⇒ TextNode.valueOf(v)
      case v: Boolean ⇒ BooleanNode.valueOf(v)
      case v: Int ⇒ IntNode.valueOf(v)
      case v: Long ⇒ LongNode.valueOf(v)
      case v: Float ⇒ FloatNode.valueOf(v)
      case v: Double ⇒ DoubleNode.valueOf(v)
      case v: BigInt ⇒ BigIntegerNode.valueOf(v.bigInteger)
      case v: BigDecimal ⇒ DecimalNode.valueOf(v.bigDecimal)
      case v ⇒ throw new IllegalArgumentException("Unsupported scalar value: " + v)
    }

    def enumNode(value: String, typeName: String): JsonNode = TextNode.valueOf(value)

    def nullNode: JsonNode = NullNode.instance

    def renderCompact(node: JsonNode): String = {
      mapper.writeValueAsString(node)
    }

    def renderPretty(node: JsonNode): String = {
      mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node)
    }
  }

  implicit object JacksonMarshallerForType extends ResultMarshallerForType[JsonNode] {
    val marshaller = JacksonResultMarshaller
  }


  implicit object JacksonInputUnmarshaller extends InputUnmarshaller[JsonNode] {
    def getRootMapValue(node: JsonNode, key: String): Option[JsonNode] = {
      Option(node.asInstanceOf[ObjectNode].get(key))
    }

    def isMapNode(node: JsonNode): Boolean = node.isObject

    def getMapValue(node: JsonNode, key: String): Option[JsonNode] = Option(node.get(key))

    def getMapKeys(node: JsonNode): Traversable[String] = node.fieldNames().asScala.toTraversable

    def isListNode(node: JsonNode): Boolean = node.isArray

    def getListValue(node: JsonNode): Seq[JsonNode] = node.elements().asScala.toSeq

    def isDefined(node: JsonNode): Boolean = !node.isNull

    // FIXME check the type of scala node
    def isScalarNode(node: JsonNode): Boolean = node match {
      case _: BooleanNode | _: NumericNode | _: TextNode => true
      case _ => false
    }

    def isEnumNode(node: JsonNode): Boolean = node.isTextual

    def isVariableNode(node: JsonNode): Boolean = false

    /**
    *   - String
    *   - Boolean
    *   - Int
    *   - Long
    *   - Float
    *   - Double
    *   - scala.BigInt
    *   - scala.BigDecimal
    *   */
    def getScalarValue(node: JsonNode): Any = node match {
      case v: BooleanNode => v.booleanValue()
      case i: IntNode => i.intValue()
      case d: DoubleNode => d.doubleValue()
      case l: LongNode => l.longValue()
      case f: FloatNode => f.floatValue()
      case i: BigIntegerNode => BigInt(i.bigIntegerValue())
      case d: DecimalNode => BigDecimal(d.decimalValue())
      case s: TextNode => s.textValue()
      case _ ⇒ throw new IllegalStateException(s"$node is not a scalar value")
    }

    def getScalaScalarValue(node: JsonNode): Any = getScalarValue(node)

    def getVariableName(node: JsonNode): String = throw new IllegalArgumentException("variables are not supported")

    def render(node: JsonNode): String = {
      mapper.writeValueAsString(node)
    }
  }

  private object JacksonToInput extends ToInput[JsonNode, JsonNode] {
    def toInput(value: JsonNode) = (value, JacksonInputUnmarshaller)
  }

  implicit def json4sJacksonToInput[T <: JsonNode]: ToInput[T, JsonNode] =
    JacksonToInput.asInstanceOf[ToInput[T, JsonNode]]

  private object Json4sJacksonFromInput extends FromInput[JsonNode] {
    val marshaller = JacksonResultMarshaller
    def fromResult(node: marshaller.Node) = node
  }

  implicit def jacksonFromInput[T <: JsonNode]: FromInput[T] =
    Json4sJacksonFromInput.asInstanceOf[FromInput[T]]

}
