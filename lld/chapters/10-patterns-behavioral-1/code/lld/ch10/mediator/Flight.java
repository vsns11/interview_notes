package lld.ch10.mediator;

record Flight(String callsign, ControlTower tower) {
    void requestLanding() { tower.requestLanding(this); }
    void landed() { tower.runwayVacated(this); }
}
