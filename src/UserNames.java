public enum UserNames {
        PETYA("Petya"),
        VASYA("Vasya"),
        SASHA("Sasha"),
        ALEX("Alex"),
        SERGEI("Sergei"),
        ANDREI("Andrei"),
        PASHA("Pasha"),
        LESHA("Lesha");


        private String name;

        UserNames(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

