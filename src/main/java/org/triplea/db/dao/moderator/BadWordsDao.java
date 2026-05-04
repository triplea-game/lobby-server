package org.triplea.db.dao.moderator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

/** DAO interface for interacting with the badword table. Essentially provides CRUD operations. */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BadWordsDao {
  private final Jdbi jdbi;

  public List<String> getBadWords() {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select word from bad_word order by word
                    """)
                .mapTo(String.class)
                .list());
  }

  public int addBadWord(String badWordToAdd) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    insert into bad_word (word) values (:word)
                    """)
                .bind("word", badWordToAdd)
                .execute());
  }

  public int removeBadWord(String badWordToRemove) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    delete from bad_word where word = :word
                    """)
                .bind("word", badWordToRemove)
                .execute());
  }

  public boolean containsBadWord(String word) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select exists (select * from bad_word where lower(:word) like '%' || lower(word) || '%')
                    """)
                .bind("word", word)
                .mapTo(Boolean.class)
                .one());
  }
}
