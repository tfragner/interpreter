package at.meroff.bac.web.rest;

import com.codahale.metrics.annotation.Timed;
import at.meroff.bac.service.CardService;
import at.meroff.bac.web.rest.errors.BadRequestAlertException;
import at.meroff.bac.web.rest.util.HeaderUtil;
import at.meroff.bac.service.dto.CardDTO;
import at.meroff.bac.service.dto.CardCriteria;
import at.meroff.bac.service.CardQueryService;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Card.
 */
@RestController
@RequestMapping("/api")
public class CardResource {

    private final Logger log = LoggerFactory.getLogger(CardResource.class);

    private static final String ENTITY_NAME = "card";

    private final CardService cardService;

    private final CardQueryService cardQueryService;

    public CardResource(CardService cardService, CardQueryService cardQueryService) {
        this.cardService = cardService;
        this.cardQueryService = cardQueryService;
    }

    /**
     * POST  /cards : Create a new card.
     *
     * @param cardDTO the cardDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new cardDTO, or with status 400 (Bad Request) if the card has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/cards")
    @Timed
    public ResponseEntity<CardDTO> createCard(@RequestBody CardDTO cardDTO) throws URISyntaxException {
        log.debug("REST request to save Card : {}", cardDTO);
        if (cardDTO.getId() != null) {
            throw new BadRequestAlertException("A new card cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CardDTO result = cardService.save(cardDTO);
        return ResponseEntity.created(new URI("/api/cards/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /cards : Updates an existing card.
     *
     * @param cardDTO the cardDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated cardDTO,
     * or with status 400 (Bad Request) if the cardDTO is not valid,
     * or with status 500 (Internal Server Error) if the cardDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/cards")
    @Timed
    public ResponseEntity<CardDTO> updateCard(@RequestBody CardDTO cardDTO) throws URISyntaxException {
        log.debug("REST request to update Card : {}", cardDTO);
        if (cardDTO.getId() == null) {
            return createCard(cardDTO);
        }
        CardDTO result = cardService.save(cardDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, cardDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /cards : get all the cards.
     *
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of cards in body
     */
    @GetMapping("/cards")
    @Timed
    public ResponseEntity<List<CardDTO>> getAllCards(CardCriteria criteria) {
        log.debug("REST request to get Cards by criteria: {}", criteria);
        List<CardDTO> entityList = cardQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * GET  /cards/:id : get the "id" card.
     *
     * @param id the id of the cardDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the cardDTO, or with status 404 (Not Found)
     */
    @GetMapping("/cards/{id}")
    @Timed
    public ResponseEntity<CardDTO> getCard(@PathVariable Long id) {
        log.debug("REST request to get Card : {}", id);
        CardDTO cardDTO = cardService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(cardDTO));
    }

    /**
     * DELETE  /cards/:id : delete the "id" card.
     *
     * @param id the id of the cardDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/cards/{id}")
    @Timed
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        log.debug("REST request to delete Card : {}", id);
        cardService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * GET  /cards : get all the cards.
     *
     * @param fieldId the field id
     * @return the ResponseEntity with status 200 (OK) and the list of cards in body
     */
    @GetMapping("/cards/byfieldid/{fieldId}")
    @Timed
    public ResponseEntity<List<CardDTO>> getAllCardsByField_Id(@PathVariable Long fieldId) {
        log.debug("REST request to get Cards by Field Id: {}", fieldId);
        List<CardDTO> entityList = cardService.findByField_Id(fieldId);
        return ResponseEntity.ok().body(entityList);
    }


}
