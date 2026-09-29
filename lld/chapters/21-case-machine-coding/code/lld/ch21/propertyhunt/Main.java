package lld.ch21.propertyhunt;

import java.util.List;

public class Main {
    static final List<String> SAMPLE_INPUT = List.of(
            "RegisterUser|Jack",
            "RegisterUser|Jill",
            "RegisterUser|Jack",
            "ListProperty|Jack|A 3BHK for sale|bellandur|90L|sell|1800 sqft|3",
            "ListProperty|Jack|A 2BHK for rent|sarjapur|40k|rent|1500 sq ft|2|bellandur,kadubeesanahalli,marathahalli",
            "ListProperty|Jill|Cosy 1BHK|HSR|25k|rent|70 sqm|1BHK",
            "ListProperty|Jill|Villa|whitefield|2.5Cr|sell|400 sqyd|4",
            "Search|Jack|null|null|null|null|null|null",
            "Search|Jack|bellandur|30000-50000|rent|150sqft|2|price",
            "Search|Jack|bellandur|null|null|null|null|price",
            "Search|Jill|null|1Cr|null|1000-2000 sqft|2,3|size",
            "Search|Jill|hsr,sarjapur|null|rent|null|1BHK,2BHK|price",
            "Shortlist|Jill|1",
            "Shortlist|Jill|2",
            "Shortlist|Jill|9",
            "Shortlist|Jack|1",
            "MarkSold|Jill|1",
            "MarkSold|Jack|1",
            "ViewShortlisted|Jill",
            "ViewListed|Jack",
            "ViewSold|Jack",
            "Search|Jill|null|null|sell|null|null|price",
            "ListProperty|Jill|Bad|x|abc|sell|100 sqft|1",
            "Search|Bob|null|null|null|null|null|null");

    public static void main(String[] args) {
        CommandProcessor cli = new CommandProcessor(new PropertyService());
        SAMPLE_INPUT.forEach(cli::execute);
    }
}
