package edu.rit.codelanx;

public class Example {

    private int one;
    private int one1;
    private int one2;
    private int one3;
    private int one4;
    private int one5;
    private int one6;
    private int one76;
    private int one78;
    private int one9;

    public Example(Builder builder) {
        this.one = builder.one;
        this.one1 = builder.one1;
    }

    public static class Builder {

        private int one;
        private int one1;
        private int one2;
        private int one3;
        private int one4;
        private int one5;
        private int one6;
        private int one76;
        private int one78;
        private int one9;


        public Builder setOne(int one) {
            this.one = one;
            return this;
        }


        public Builder setOne1(int one1) {
            this.one1 = one1;
            return this;
        }


        public Builder setOne2(int one2) {
            this.one2 = one2;
            return this;
        }

        public Example build() {
            return new Example(this);
        }
    }

}
