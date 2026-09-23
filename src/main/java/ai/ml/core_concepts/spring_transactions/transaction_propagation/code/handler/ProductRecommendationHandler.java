package ai.ml.core_concepts.spring_transactions.transaction_propagation.code.handler;
import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductRecommendationHandler {

    //Fetch product recommendation (NOT_SUPPORTED)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Product> getRecommendation() {

        //simulate hard coded product recommendation
        List<Product> recommendations = new ArrayList<>();
        recommendations.add(new Product(101,"Mobile", 123.12,10));
        recommendations.add(new Product(102,"Watch", 321.12,12));
        recommendations.add(new Product(103,"Bottle", 233.12,11));
        recommendations.add(new Product(104,"Fridge", 871.12,15));

        System.out.println("Recommendation fetch for customer");
        return recommendations;
    }

}
