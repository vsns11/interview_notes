package lld.ch07.worked.step5;

import static lld.ch07.worked.step5.StatementRenderer.money;

/** The NEW feature. Thanks to Split Phase it reuses the calculator untouched. */
public final class HtmlRenderer implements StatementRenderer {

    @Override
    public String render(StatementData data) {
        StringBuilder result = new StringBuilder("<h1>Invoice for " + data.customer() + "</h1>\n<table>\n");
        for (StatementData.Line line : data.lines()) {
            result.append("  <tr><td>").append(line.type().code()).append("</td><td>")
                  .append(line.weightKg()).append("kg</td><td>").append(line.distanceKm())
                  .append("km</td><td>").append(money(line.amount())).append("</td></tr>\n");
        }
        result.append("</table>\n<p>Amount owed: <b>").append(money(data.totalAmount())).append("</b></p>\n");
        result.append("<p>Points earned: <b>").append(data.totalPoints()).append("</b></p>\n");
        return result.toString();
    }
}
