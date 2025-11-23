-- Script to update all clubs to have the same city as their stadium
UPDATE clubes c
SET cidade = e.cidade
FROM estadios e
WHERE c.estadio_id = e.id
  AND (c.cidade IS NULL OR c.cidade != e.cidade);
