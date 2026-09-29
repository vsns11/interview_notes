package lld.ch11.templatemethod;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Import jobs ===");
        new CsvImportJob().run("id,name\nu1, Asha\nu2, Ben\nu3, Chen");
        new JsonImportJob().run("[{\"id\":\"p9\",\"qty\":1},{\"id\":\"p7\",\"qty\":4}]");

        System.out.println("\n=== 2. Beverages (hook controls an optional step) ===");
        System.out.println("tea:");
        new Tea().prepareRecipe();
        System.out.println("black coffee:");
        new Coffee(true).prepareRecipe();
    }
}
