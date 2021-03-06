/*
 * Copyright 2015 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.heikoseeberger.akkasse

import akka.stream.scaladsl.{ Sink, Source }
import akka.util.ByteString

class LineParserSpec extends BaseSpec {

  "A LineParser" should {

    "parse lines terminated with either CR, LF or CRLF" in {
      val input1 = "line1\nline2\rline3\r"
      val input2 = "\nline4\nline5\rline6\r\n\n"
      Source(Vector(ByteString(input1), ByteString(input2)))
        .via(new LineParser(1048576))
        .runWith(Sink.seq)
        .map(_ shouldBe Vector("line1", "line2", "line3", "line4", "line5", "line6", ""))
    }

    "ignore a trailing non-terminated line" in {
      val input = "line1\nline2\rline3\r\nline4\nline5\rline6\r\n\nincomplete"
      Source
        .single(ByteString(input))
        .via(new LineParser(1048576))
        .runWith(Sink.seq)
        .map(_ shouldBe Vector("line1", "line2", "line3", "line4", "line5", "line6", ""))
    }

    "handle splitted line" in {
      val testLine = "test line"
      val input    = s"$testLine\n".grouped(1).map(ByteString.apply).toVector
      Source(input).via(new LineParser(1048576)).runWith(Sink.seq).map(_ shouldBe Seq(testLine))
    }
  }
}
