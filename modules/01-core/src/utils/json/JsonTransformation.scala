package utils.json

import play.api.libs.json._

import scala.annotation.tailrec

object JsonTransformation {

  /**
   * This method transform a JsValue to a valid JsValue
   */
  private[this] def jsValue_parser(value: JsValue, f: JsObject => JsObject): JsValue = {
    @tailrec
    def jsValue_parserR(value: JsValue, f: JsObject => JsObject, acc: JsArray = JsArray.empty): JsValue = {
      value match {
        case _: JsBoolean | _: JsNumber | _: JsString => value
        case JsArray(seq) if seq.nonEmpty             =>
          val head_parser = seq.head match {
            case _: JsBoolean | _: JsNumber | _: JsString => seq.head
            case x: JsObject                              => f(x)
            case _                                        => JsNull
          }
          if(seq.tail.nonEmpty) jsValue_parserR(JsArray(seq.tail), f, acc.:+(head_parser)) else acc.:+(head_parser)
        case x: JsObject                              => f(x)
        case _                                        => JsNull
      }
    }
    jsValue_parserR(value, f)
  }

  /**
   * This method transform a jsObject which have nested object into keys with '.', like this example:
   *  @from :
   *  {
   *    "first": {"one": "1"},
   *    "second[0]": {"two": "2"}
   *  }
   *  @to
   *  {
   *    "first.one": "1",
   *    "second[0].two": "2"
   *  }
   *
   *  It's the reverse of jsObject_mapping
   */
  def jsObject_flatten(data: JsObject): JsObject = {

    def inner_jsObject_flatten(data: JsValue, maybe_prefix: Option[String] = None): Seq[JsObject] = {
      val prefix = maybe_prefix.map(p => p + ".").getOrElse("")
      data match {
        case obj: JsObject => {
          obj.fields.toList.flatMap { case (label, value) => inner_jsObject_flatten(value, Some(prefix + label)) }
        }
        case f             => List(Json.obj(maybe_prefix.getOrElse("") -> f))
      }
    }

    data.value match {
      case a_map if (a_map.isEmpty) => data
      case _                        => inner_jsObject_flatten(data).reduceOption(_ ++ _).getOrElse(Json.obj())
    }
  }

  /**
   * This method transform a jsObject which have keys with '.', to a jsObject which have a JsObject values, like this example:
   *  @from :
   *  {
   *    "first.one": "1",
   *    "second[0].two": "2"
   *  }
   *  @to
   * {
   *   "first": {"one": "1"},
   *   "second[0]": {"two": "2"}
   * }
   */
  def jsObject_mapping(data: JsObject): JsObject = {
    val (keys_with_dot, keys_without_dot) = data.fields.partition(_._1.contains('.'))
    (keys_with_dot, keys_without_dot) match {
      case (Nil, Nil)              => data
      case (Nil, without_dot)      =>
        if(without_dot.exists(_._2.toString.contains('.'))) {
          val new_data = without_dot.map { case (k, v) => k -> jsValue_parser(v, jsObject_mapping) }
          JsObject(new_data)
        } else {
          JsObject(without_dot)
        }
      case (with_dot, without_dot) =>
        val new_data = with_dot
          .map {
            case (k, v) =>
              (k.split('.').head, k.substring(k.indexOf('.') + 1) -> v)
          }
          .groupBy(_._1)
          .map { case (k, v) => k -> JsObject(v.map(_._2)) }
        JsObject(without_dot).deepMerge(jsObject_mapping(JsObject(new_data)))
      case _                       => data
    }
  }

  /**
   * This method transform a jsObject which have a keys with '[number]', to jsObject which have a jsArray values ,  like this example:
   *
   *  @from :
   *  {
   *    "first": {"one": "1"},
   *    "second[0]": {"two": "2"},
   *    "second[1]": {"three": "3"}
   *  }
   *  @to
   * {
   *   "first":{"one":"1"},
   *   "second":[{"two":"2"}, {"three": "3"}]
   * }
   */
  def jsArray_mapping(data: JsObject): JsObject = {
    val (keys_with_bracket, keys_without_bracket) = data.fields.sortBy(_._1).partition(a => a._1.contains("["))
    val with_key_mapped                           = (keys_with_bracket, keys_without_bracket) match {
      case (Nil, _)                        => data
      case (with_bracket, without_bracket) =>
        val new_data = with_bracket
          .map {
            case (k, v) =>
              (k.substring(0, k.indexOf('[')), k.substring(0, k.indexOf('[')) -> v)
          }
          .groupBy(_._1)
          .map { case (k, v) => k -> JsArray(v.map(_._2._2)) }
        JsObject(without_bracket).deepMerge(jsArray_mapping(JsObject(new_data)))
      case _                               => data
    }

    val with_values_mapped = with_key_mapped.fields.map { case (k, v) => k -> jsValue_parser(v, jsArray_mapping) }
    JsObject(with_values_mapped)
  }

  /**
   * This method transform a jsObject which have a jsArray values, to jsObject which have a keys with '[number]',  like this example:
   *
   *  @from
   * {
   *   "first":{"one":"1"},
   *   "second":[{"two":"2"}, {"three": "3"}]
   * }
   *  @to
   *  {
   *    "first": {"one": "1"},
   *    "second[0]": {"two": "2"},
   *    "second[1]": {"three": "3"}
   *  }
   */
  def jsArray_flatten(data: JsObject): JsObject = {
    val with_key_mapped = data.value
      .flatMap[String, JsValue] {
        case (k, v) =>
          v match {
            case JsArray(value) => {
              value.zipWithIndex.map { case (json_v, index) => s"${k}[$index]" -> json_v }.toMap
            }
            case _              => Map(k -> v)
          }
      }

    val with_values_mapped = with_key_mapped.map[String, JsValue] { case (k, v) =>
      k -> jsValue_parser(v, jsArray_flatten)
    }
    JsObject(with_values_mapped)
  }

  def mapValue(mapping: Map[String, String])(data: JsObject): JsObject = {

    /**
     * This is not tailrec but we do a little test in PricerDataOperationTest
     * to ensure it won't blowup stack
     */
    def mapJsValue(json: JsValue): JsValue = {
      json match {
        case obj: JsObject     => JsObject(
            obj.value.view.mapValues(mapJsValue).toMap
          )
        case JsString(a_value) => JsString(mapping.getOrElse(a_value, a_value))
        case array: JsArray    => JsArray(array.value.toSeq.map(mapJsValue))
        case x                 => x
      }
    }

    // we avoid cast by not using mapJsValue(data).as[JsObject]
    JsObject(
      data.value.view.mapValues(mapJsValue).toMap
    )
  }

  /**
   * This is not tailrec but we do a little test in PricerDataOperationTest
   * to ensure it won't blowup stack
   */
  def mapKey(mapping: Map[String, String])(data: JsObject): JsObject = {
    val new_data = data.value
      .map[String, JsValue] {
        case (k, v) => mapping.getOrElse(k, k) -> v
      }
      .map[String, JsValue] {
        case (k, v) =>
          v match {
            case obj: JsObject => k -> mapKey(mapping)(obj)
            case arr: JsArray  => {
              val arr_mapped = arr.value.toList.map {
                case obj: JsObject => mapKey(mapping)(obj)
                case x             => x
              }

              k -> JsArray(arr_mapped)
            }
            case _             => k -> v
          }
      }
    JsObject(new_data)
  }
}
