package ai.ml.core_concepts.java_features_8_11_16_17_21.java__16.records.code.usecase_1;

public record User(Long id, String name) {

    public User{
        if(name==null){
            throw  new IllegalArgumentException("Name can not be null");
        }
        name = name.trim();
    }
}
