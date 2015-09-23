package model

import scroll.internal.Compartment

object SmartHome {

  class SmartPhone

  class Camera

  class SmartWatch

  class Speaker

  class Person

  class Room extends Compartment {

    class Sensor

    class Actuator

    class Inhabitant

    class Process

  }

  class EmergencyCall extends Compartment {

    class Patient

    class EmergencyCaller

    class HealthMonitor

    class Injured

  }

  class InteractiveEmergencyCall extends EmergencyCall {

    class UnresponsiveInjured

    class Requester

    class Injured

    class Responder

  }

}
