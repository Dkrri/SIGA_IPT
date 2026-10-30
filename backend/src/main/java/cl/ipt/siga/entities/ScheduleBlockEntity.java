package cl.ipt.siga.entities;

import cl.ipt.siga.enums.Weekday;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "schedule_blocks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"section_id", "weekday", "module"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Schedule block entity
public class ScheduleBlockEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Weekday (L/M/W/J/V)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Weekday weekday;

    // Module 1 to 6
    @Column(nullable = false)
    private Integer module;

    // Section it belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;
}
