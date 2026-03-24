-- Authors
INSERT INTO authors (name, bio) VALUES
    ('George Orwell', 'English novelist and essayist, journalist and critic, best known for the allegorical novella Animal Farm and the dystopian novel Nineteen Eighty-Four.'),
    ('J.K. Rowling', 'British author best known for the Harry Potter fantasy series, which has won multiple awards and sold more than 500 million copies.'),
    ('F. Scott Fitzgerald', 'American novelist and short story writer, widely regarded as one of the greatest American writers of the 20th century.'),
    ('Harper Lee', 'American novelist best known for her 1960 novel To Kill a Mockingbird, which won the 1961 Pulitzer Prize.'),
    ('J.R.R. Tolkien', 'English writer, poet, philologist, and academic, best known as the author of the high fantasy works The Hobbit and The Lord of the Rings.');

-- Categories
INSERT INTO categories (name, description) VALUES
    ('Fiction', 'Literary works created from the imagination, not presented as fact.'),
    ('Fantasy', 'A genre of speculative fiction set in a fictional universe, often inspired by real world myth and folklore.'),
    ('Classic Literature', 'A work of literature that is generally accepted as exemplifying quality and has stood the test of time.'),
    ('Science Fiction', 'A genre of speculative fiction dealing with imaginative and futuristic concepts such as advanced science and technology.');

-- Books (no author_id / category_id — those live in join tables)
INSERT INTO books (title, description, price, isbn, published_date, stock) VALUES
    ('1984', 'A dystopian social science fiction novel and cautionary tale about the dangers of totalitarianism.', 12.99, '978-0451524935', '1949-06-08', 150),
    ('Animal Farm', 'An allegorical novella reflecting events leading up to the Russian Revolution and the Stalinist era.', 9.99, '978-0451526342', '1945-08-17', 120),
    ('Harry Potter and the Philosopher''s Stone', 'The first novel in the Harry Potter series, following a young wizard Harry Potter who discovers his magical heritage.', 14.99, '978-0747532699', '1997-06-26', 200),
    ('Harry Potter and the Chamber of Secrets', 'The second novel in the Harry Potter series, in which Harry and friends investigate a series of attacks at Hogwarts.', 14.99, '978-0747538493', '1998-07-02', 180),
    ('The Great Gatsby', 'A 1925 novel set in the Jazz Age on Long Island, near New York City, critiquing the American Dream.', 10.99, '978-0743273565', '1925-04-10', 90),
    ('Tender Is the Night', 'The fourth and final completed novel by F. Scott Fitzgerald, exploring the rise and fall of Dick Diver.', 11.99, '978-0684801544', '1934-04-12', 60),
    ('To Kill a Mockingbird', 'A novel set in the American South during the 1930s, dealing with the serious issues of rape and racial inequality.', 13.99, '978-0061935466', '1960-07-11', 175),
    ('The Hobbit', 'A children''s fantasy novel by J.R.R. Tolkien, the prequel to The Lord of the Rings.', 13.99, '978-0618260300', '1937-09-21', 130),
    ('The Fellowship of the Ring', 'The first volume of The Lord of the Rings trilogy, following Frodo Baggins on his quest to destroy the One Ring.', 15.99, '978-0618346257', '1954-07-29', 110),
    ('The Two Towers', 'The second volume of The Lord of the Rings trilogy, following the fellowship as it splits into separate groups.', 15.99, '978-0618346264', '1954-11-11', 100),
    ('The Return of the King', 'The third and final volume of The Lord of the Rings trilogy, concluding the journey to destroy the One Ring.', 15.99, '978-0618346271', '1955-10-20', 95);

-- Book-Author associations
INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-0451524935' AND a.name = 'George Orwell';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-0451526342' AND a.name = 'George Orwell';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-0747532699' AND a.name = 'J.K. Rowling';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-0747538493' AND a.name = 'J.K. Rowling';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-0743273565' AND a.name = 'F. Scott Fitzgerald';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-0684801544' AND a.name = 'F. Scott Fitzgerald';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-0061935466' AND a.name = 'Harper Lee';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-0618260300' AND a.name = 'J.R.R. Tolkien';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-0618346257' AND a.name = 'J.R.R. Tolkien';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-0618346264' AND a.name = 'J.R.R. Tolkien';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-0618346271' AND a.name = 'J.R.R. Tolkien';

-- Book-Category associations
INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-0451524935' AND c.name = 'Science Fiction';

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-0451526342' AND c.name = 'Fiction';

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-0747532699' AND c.name = 'Fantasy';

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-0747538493' AND c.name = 'Fantasy';

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-0743273565' AND c.name = 'Classic Literature';

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-0684801544' AND c.name = 'Classic Literature';

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-0061935466' AND c.name = 'Classic Literature';

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-0618260300' AND c.name = 'Fantasy';

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-0618346257' AND c.name = 'Fantasy';

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-0618346264' AND c.name = 'Fantasy';

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-0618346271' AND c.name = 'Fantasy';
