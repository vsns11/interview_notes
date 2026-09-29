package lld.ch10.mediator;

final class CountrySelect extends Component {
    private String selected;

    void select(String country) {
        selected = country;
        System.out.println("  [country] user picked " + country);
        mediator.notify(this, country);          // report up; never call a sibling
    }

    String selected() { return selected; }
}
