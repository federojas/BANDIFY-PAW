package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Audition;
import ar.edu.itba.paw.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class AuditionJdbcDao implements AuditionDao {

    private JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final static RowMapper<Audition> ROW_MAPPER = (rs, i) -> {
        Array mAux = rs.getArray("musicGenres");
        String[] musicGenresArray = (String[])mAux.getArray();
        List<String> musicGenres = Arrays.asList(musicGenresArray);

        Array lAux = rs.getArray("lookingFor");
        String[] lookingForArray = (String[])lAux.getArray();
        List<String> lookingFor = Arrays.asList(lookingForArray);

        return new Audition(rs.getLong("id"),
                rs.getLong("bandId"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("location"),
                rs.getDate("creationDAte"),
                musicGenres,
                lookingFor);
    };


    @Autowired
    public AuditionJdbcDao(final DataSource ds) {
        jdbcTemplate = new JdbcTemplate(ds);
        jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("auditions").usingGeneratedKeyColumns("id");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS auditions  " +
            "("
            + "id SERIAL PRIMARY KEY,"
            + "bandId INT NOT NULL,"
            + "title VARCHAR(100) UNIQUE NOT NULL,"
            + "description VARCHAR(512) NOT NULL,"
            + "creationDate DATE NOT NULL,"
            + "location VARCHAR(100) NOT NULL,"
            + "musicGenres TEXT[] NOT NULL,"
            + "lookingFor TEXT[] NOT NULL"
            + ")"
            // TODO : FOREIGN KEY(band_id) REFERENCES Band(band_id) ON DELETE CASCADE
        );
    }

    @Override
    public Optional<Audition> getAuditionById(long id) {
        final List<Audition> list = jdbcTemplate.query("SELECT * FROM auditions WHERE id = ?", new Object[]{id}, ROW_MAPPER);
        return list.stream().findFirst();
    }

    @Override
    public Audition create(String title, String description, String location, Date creationDate, List<String> musicGenres, List<String> lookingFor) {
        final Map<String, Object> auditionData = new HashMap<>();
        auditionData.put("title", title);
        auditionData.put("description", description);
        auditionData.put("location", location);
        auditionData.put("date", creationDate);
        auditionData.put("musicGenres", musicGenres);
        auditionData.put("lookingFor", lookingFor);
        final Number id = jdbcInsert.executeAndReturnKey(auditionData);
        // TODO: BandId?
        return new Audition(id.longValue(),3, title, description, location, creationDate, musicGenres, lookingFor);
    }

    @Override
    public List<Audition> getAll(int page) {
        return jdbcTemplate.query("SELECT * FROM auditions LIMIT 10 OFFSET ?", new Object[] { (page -1) * 10}, ROW_MAPPER);
    }
}