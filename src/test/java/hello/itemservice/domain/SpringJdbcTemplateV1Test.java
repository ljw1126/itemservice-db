package hello.itemservice.domain;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@Transactional
public class SpringJdbcTemplateV1Test {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        save(new Actor(1L, "Lee", "jinwoo"));
        save(new Actor(2L, "firstName", "lastName"));
    }

    @DisplayName("")
    @Test
    void count() {
        //given when
        int count = this.jdbcTemplate.queryForObject("select count(*) from actor", Integer.class);

        //then
        assertThat(count).isEqualTo(2);
    }

    @DisplayName("")
    @Test
    void countByFirstName() {
        //given when
        String sql = "select count(*) from actor where first_name = ?";
        String queryParam = "Lee";
        int count = this.jdbcTemplate.queryForObject(sql, Integer.class, queryParam);

        //then
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("")
    @Test
    void selectLastNameById_ReturnTypeString() {
        //given when
        String sql = "select last_name from actor where id = ?";
        long param = 1L;
        String lastName = this.jdbcTemplate.queryForObject(sql, String.class, param);

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
        String sql = "select * from actor";
        List<Actor> actors = this.jdbcTemplate.query(sql, actorRowMapper());

        //then
        assertThat(actors).hasSize(2);
    }

    @DisplayName("")
    @Test
    void update() {
        //given, when
        String sql = "update actor set last_name = ? where id = ?";
        this.jdbcTemplate.update(sql, "JinWoo", 1L);

        Actor actor = findById(1L).get();

        //then
        assertThat(actor.getLastName()).isEqualTo("JinWoo");
    }

    @DisplayName("")
    @Test
    void delete() {
        //given, when
        String sql = "delete from actor where id = ?";
        this.jdbcTemplate.update(sql, 1L);

        //then
        assertThatThrownBy(() -> findById(1L).get()).isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("")
    @Test
    void queryForList() {
        List<Map<String, Object>> resultMap = this.jdbcTemplate.queryForList("select * from actor");
        assertThat(resultMap).hasSize(2)
                .extracting("FIRST_NAME", "LAST_NAME")
                .containsExactly(Tuple.tuple("Lee", "jinwoo"), Tuple.tuple("firstName", "lastName"));
    }

    private static RowMapper<Actor> actorRowMapper() {
        return (rs, rowNum) -> {
            Actor actor = new Actor();
            actor.setId(rs.getLong("id"));
            actor.setFirstName(rs.getString("first_name"));
            actor.setLastName(rs.getString("last_name"));
            return actor;
        };
    }

    private Actor save(Actor actor) {
        String sql = "insert into actor(id, first_name, last_name) values(?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"});
            ps.setLong(1, actor.getId());
            ps.setString(2, actor.getFirstName());
            ps.setString(3, actor.getLastName());
            return ps;
        }, keyHolder);

        long key = keyHolder.getKey().longValue();
        actor.setId(key);
        return actor;
    }

    private Optional<Actor> findById(Long id) {
        String sql = "select id, first_name, last_name from actor where id = ?";
        try {
            return Optional.of(this.jdbcTemplate.queryForObject(sql, actorRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            log.error("error = {}", e.getMessage());
            return Optional.empty();
        }
    }
}
