package hello.itemservice.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@Transactional
public class SpringJdbcTemplateV2Test {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        save(new Actor(1L, "Lee", "jinwoo"));
        save(new Actor(2L, "firstName", "lastName"));
    }

    @DisplayName("")
    @Test
    void count() {
        //given when
        int count = this.jdbcTemplate.queryForObject("select count(*) from actor", new HashMap<>(),Integer.class);

        //then
        assertThat(count).isEqualTo(2);
    }

    @DisplayName("")
    @Test
    void countByFirstName() {
        //given when
        String sql = "select count(*) from actor where first_name = :firstName";
        int count = this.jdbcTemplate.queryForObject(sql, Map.of("firstName", "Lee"), Integer.class);

        //then
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("")
    @Test
    void selectLastNameById_ReturnTypeString() {
        //given when
        String sql = "select last_name from actor where id = :id";
        String lastName = this.jdbcTemplate.queryForObject(sql, Map.of("id", 1L),String.class);

        //then
        assertThat(lastName).isEqualTo("jinwoo");
    }

    @DisplayName("")
    @Test
    void findById() {
        //given when
        long givenId = 1L;
        Actor result = findById(givenId).get();

        // then
        assertThat(result).extracting("id", "firstName", "lastName")
                .containsExactly(1L, "Lee", "jinwoo");
    }

    @DisplayName("")
    @Test
    void findAll() {
        //given, when
        Actor searchCondDto = new Actor(); // dto 쓰기
        searchCondDto.setFirstName("Lee");
        searchCondDto.setLastName("jinwoo");

        String sql = "select * from actor where first_name = :firstName and last_name = :lastName";
        SqlParameterSource param = new BeanPropertySqlParameterSource(searchCondDto);
        List<Actor> actors = this.jdbcTemplate.query(sql, param, actorRowMapper());

        //then
        assertThat(actors).hasSize(1);
    }

    @DisplayName("")
    @Test
    void update() {
        //given, when
        String sql = "update actor set last_name = :lastName where id = :id";

        //Map<String, Object> param = Map.of("lastName", "JinWoo", "id", 1L);
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("lastName", "JinWoo")
                .addValue("id", 1L);

        this.jdbcTemplate.update(sql, param);

        Actor actor = findById(1L).get();

        //then
        assertThat(actor.getLastName()).isEqualTo("JinWoo");
    }

    @DisplayName("")
    @Test
    void delete() {
        //given, when
        String sql = "delete from actor where id = :id";
        Map<String, Object> param = Map.of("id", 1L);
        this.jdbcTemplate.update(sql, param);

        //then
        assertThatThrownBy(() -> findById(1L).get()).isInstanceOf(NoSuchElementException.class);
    }

    private static RowMapper<Actor> actorRowMapper() {
        return new BeanPropertyRowMapper<>(Actor.class);
    }

    private Actor save(Actor actor) {
        String sql = "insert into actor(id, first_name, last_name) values(:id, :firstName, :lastName)";

        SqlParameterSource param = new BeanPropertySqlParameterSource(actor);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, param, keyHolder);

        long key = keyHolder.getKey().longValue();
        actor.setId(key);
        return actor;
    }

    private Optional<Actor> findById(Long id) {
        String sql = "select id, first_name, last_name from actor where id = :id";
        try {
            Map<String, Object> param = Map.of("id", id);
            return Optional.of(this.jdbcTemplate.queryForObject(sql, param, actorRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            log.error("error = {}", e.getMessage());
            return Optional.empty();
        }
    }
}
