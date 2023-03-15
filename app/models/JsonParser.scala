package models

import domain._

import event._

trait JsonParser extends UserJsonParser with EventJsonParser
