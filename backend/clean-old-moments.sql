-- Delete old moments that have non-UUID image filenames
-- UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
DELETE FROM moment WHERE image_url NOT LIKE '%-%-%-%-%';

-- Or if you want to keep only moments with .jpeg extension and UUID format
-- DELETE FROM moment WHERE image_url NOT LIKE '________-____-____-____-____________.jpeg';
