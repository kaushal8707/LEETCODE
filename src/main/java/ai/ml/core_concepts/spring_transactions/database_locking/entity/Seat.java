package ai.ml.core_concepts.spring_transactions.database_locking.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String movieName;

    private boolean booked;

    @Version
    private int version;
}