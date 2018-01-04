package at.meroff.bac.domain;

import at.meroff.bac.domain.enumeration.CardType;
import at.meroff.bac.helper.Calculation;
import at.meroff.bac.helper.Statistics;
import javafx.util.Pair;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import at.meroff.bac.domain.enumeration.LayoutType;

/**
 * A Field.
 */
@Entity
@Table(name = "field")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Field implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "orig_image")
    private byte[] origImage;

    @Column(name = "orig_image_content_type")
    private String origImageContentType;

    @Lob
    @Column(name = "svg_image")
    private byte[] svgImage;

    @Column(name = "svg_image_content_type")
    private String svgImageContentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "layout_type")
    private LayoutType layoutType;

    @OneToMany(mappedBy = "field")
    //@JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Card> cards = new HashSet<>();

    @Transient
    private Set<Pair<Card, Set<Pair<Card, Set<Pair<Card, Calculation>>>>>> preCalculatedValues;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public Field description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getOrigImage() {
        return origImage;
    }

    public Field origImage(byte[] origImage) {
        this.origImage = origImage;
        return this;
    }

    public void setOrigImage(byte[] origImage) {
        this.origImage = origImage;
    }

    public String getOrigImageContentType() {
        return origImageContentType;
    }

    public Field origImageContentType(String origImageContentType) {
        this.origImageContentType = origImageContentType;
        return this;
    }

    public void setOrigImageContentType(String origImageContentType) {
        this.origImageContentType = origImageContentType;
    }

    public byte[] getSvgImage() {
        return svgImage;
    }

    public Field svgImage(byte[] svgImage) {
        this.svgImage = svgImage;
        return this;
    }

    public void setSvgImage(byte[] svgImage) {
        this.svgImage = svgImage;
    }

    public String getSvgImageContentType() {
        return svgImageContentType;
    }

    public Field svgImageContentType(String svgImageContentType) {
        this.svgImageContentType = svgImageContentType;
        return this;
    }

    public void setSvgImageContentType(String svgImageContentType) {
        this.svgImageContentType = svgImageContentType;
    }

    public LayoutType getLayoutType() {
        return layoutType;
    }

    public Field layoutType(LayoutType layoutType) {
        this.layoutType = layoutType;
        return this;
    }

    public void setLayoutType(LayoutType layoutType) {
        this.layoutType = layoutType;
    }

    public Set<Card> getCards() {
        return cards;
    }

    public Field cards(Set<Card> cards) {
        this.cards = cards;
        return this;
    }

    public Field addCard(Card card) {
        this.cards.add(card);
        card.setField(this);
        return this;
    }

    public Field removeCard(Card card) {
        this.cards.remove(card);
        card.setField(null);
        return this;
    }

    public void setCards(Set<Card> cards) {
        this.cards = cards;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Field field = (Field) o;
        if (field.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), field.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Field{" +
            "id=" + getId() +
            ", description='" + getDescription() + "'" +
            ", origImage='" + getOrigImage() + "'" +
            ", origImageContentType='" + getOrigImageContentType() + "'" +
            ", svgImage='" + getSvgImage() + "'" +
            ", svgImageContentType='" + getSvgImageContentType() + "'" +
            ", layoutType='" + getLayoutType() + "'" +
            "}";
    }

    public void findRelations() {
        if (checkForStarLayout()) {
            setLayoutType(LayoutType.STAR);
        } else {
            setLayoutType(LayoutType.DEFAULT);

            // Reset task assignment
            getCards().forEach(card -> card.setSubject(null));
            getCards().clear();
        }

        preCalculateValues();

    }


    private boolean checkForStarLayout() {
        Set<Card> subjects = new HashSet<>(
            this.cards.stream()
                .filter(card -> card.getCardType().equals(CardType.SUBJECT)).collect(Collectors.toSet())
        );
        Set<Card> tasks = new HashSet<>(
            this.cards.stream()
                .filter(card -> card.getCardType().equals(CardType.TASK)).collect(Collectors.toSet())
        );

        Set<Pair<Card, Set<Card>>> summary = subjects.stream()
            .map(subject -> {
                return new Pair<>(subject, tasks.stream()
                    .map(task -> {
                        return new Pair<>(task, subjects.stream()
                            .map(subject1 -> new Pair<>(subject1, Card.getDistance(task, subject1)))
                            .min(Comparator.comparingDouble(Pair::getValue))
                            .map(Pair::getKey)
                            .orElseThrow(() -> new IllegalStateException("blabla")));
                    }).filter(taskSubjectPair -> taskSubjectPair.getValue().equals(subject))
                    .map(Pair::getKey)
                    .collect(Collectors.toSet()));
            }).collect(Collectors.toSet());

        List<Card> ret = summary.stream()
            .map(subjectSetPair -> {
                Card key = subjectSetPair.getKey();
                subjectSetPair.getValue().stream()
                    .sorted(Comparator.comparingDouble(value -> Card.getDistance(key,value)))
                    .forEach(task -> {
                        key.getTasks().add(task);
                        task.setSubject(key);
                    });
                return key;
            }).collect(Collectors.toList());


        OptionalDouble average = summary.stream()
            .filter(subjectSetPair -> subjectSetPair.getValue().size() > 1)
            .map(subjectSetPair -> {
                double[] doubles = subjectSetPair.getValue().stream()
                    .mapToDouble(value -> Card.getDistance(subjectSetPair.getKey(), value)).toArray();
                Statistics statistics = new Statistics(doubles);
                return statistics.bbb();
            }).mapToDouble(value -> value)
            .average();
        System.out.println("Variationskoeffizient: " + average);

        if (average.isPresent()) {
            if (average.getAsDouble() < 0.3) {
                System.out.println("Sternmuster entdeckt!!!");
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void preCalculateValues() {
        this.preCalculatedValues = cards.stream()
            .filter(card -> card.getCardType().equals(CardType.SUBJECT))
            .map(subject -> new Pair<>(subject, cards.stream()
                .filter(card -> card.getCardType().equals(CardType.TASK))
                .map(sourceTask -> new Pair<>(sourceTask, cards.stream()
                    .filter(card -> card.getCardType().equals(CardType.TASK))
                    .map(targetTask -> new Pair<>(targetTask, new Calculation(subject, sourceTask, targetTask)))
                    .collect(Collectors.toSet()))
                ).collect(Collectors.toSet())))
            .collect(Collectors.toSet());
    }
}
