package at.meroff.bac.service.mapper;

import at.meroff.bac.domain.*;
import at.meroff.bac.service.dto.CardDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Card and its DTO CardDTO.
 */
@Mapper(componentModel = "spring", uses = {FieldMapper.class})
public interface CardMapper extends EntityMapper<CardDTO, Card> {

    @Mapping(source = "field.id", target = "fieldId")
    @Mapping(source = "subject.id", target = "subjectId")
    CardDTO toDto(Card card);

    @Mapping(source = "fieldId", target = "field")
    @Mapping(target = "tasks", ignore = true)
    @Mapping(source = "subjectId", target = "subject")
    Card toEntity(CardDTO cardDTO);

    default Card fromId(Long id) {
        if (id == null) {
            return null;
        }
        Card card = new Card();
        card.setId(id);
        return card;
    }
}
