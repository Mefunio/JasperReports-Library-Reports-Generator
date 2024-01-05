create table Autor
(
    ID_Autora int auto_increment
        primary key,
    Imie      varchar(50) null,
    Nazwisko  varchar(50) null
);

create table Czytelnik
(
    ID_Czytelnika int auto_increment
        primary key,
    Imie          varchar(50) null,
    Nazwisko      varchar(50) null,
    Telefon       varchar(50) not null
);

create definer = root@localhost trigger before_insert_czytelnik
    before insert
    on Czytelnik
    for each row
BEGIN
    IF NEW.Telefon IS NULL THEN
        SET NEW.Telefon = 'Not Provided';
    END IF;
END;

create table Jezyk
(
    ID_Jezyka int auto_increment
        primary key,
    Nazwa     varchar(50) null
);

create table Kategoria
(
    ID_Kategorii           int auto_increment
        primary key,
    Nazwa                  varchar(50) null,
    ID_NadrzednejKategorii int         null,
    constraint kat_nad
        foreign key (ID_NadrzednejKategorii) references Kategoria (ID_Kategorii)
);

create table Lokalizacja
(
    ID_Lokalizacji int auto_increment
        primary key,
    Miasto         varchar(50)  null,
    Adres          varchar(255) null
);

create table Pracownik
(
    ID_Pracownika  int auto_increment
        primary key,
    Imie           varchar(50) null,
    Nazwisko       varchar(50) null,
    ID_Lokalizacji int         null,
    constraint lok_lok
        foreign key (ID_Lokalizacji) references Lokalizacja (ID_Lokalizacji)
);

create table Bibliotekarz
(
    ID_Pracownika int not null,
    Staz          int null,
    constraint prac_bibl
        foreign key (ID_Pracownika) references Pracownik (ID_Pracownika)
);

create table Magazynier
(
    ID_Pracownika  int not null,
    Numer_Telefonu int null,
    constraint prac_kier
        foreign key (ID_Pracownika) references Pracownik (ID_Pracownika)
);

create table Ochroniarz
(
    ID_Pracownika  int not null,
    Poziom_Dostepu int null,
    constraint prac_ochrn
        foreign key (ID_Pracownika) references Pracownik (ID_Pracownika)
);

create table Recenzja
(
    ID_Recenzji int auto_increment
        primary key,
    Ocena       int          null,
    Opinia      varchar(200) null
);

create table Wydawnictwo
(
    ID_Wydawnictwa int auto_increment
        primary key,
    Nazwa          varchar(100) null,
    Adres          varchar(255) null
);

create table Ksiazka
(
    ID_Ksiazki     int auto_increment
        primary key,
    Tytul          varchar(255) null,
    ID_Autora      int          null,
    ID_Kategorii   int          null,
    ID_Wydawnictwa int          null,
    Rok_Wydania    int          null,
    Dostepnosc     bit          not null,
    Ilosc          int          null,
    constraint aut_aut
        foreign key (ID_Autora) references Autor (ID_Autora),
    constraint kat_kat
        foreign key (ID_Kategorii) references Kategoria (ID_Kategorii),
    constraint wyd_wyd
        foreign key (ID_Wydawnictwa) references Wydawnictwo (ID_Wydawnictwa)
);

create definer = root@localhost trigger before_insert_update_ksiazka
    before insert
    on Ksiazka
    for each row
BEGIN
    IF NEW.Ilosc > 0 THEN
        SET NEW.Dostepnosc = 1; -- Ustaw dostępność na true
    ELSE
        SET NEW.Dostepnosc = 0; -- Ustaw dostępność na false
    END IF;
END;

create definer = root@localhost trigger before_update_ksiazka
    before update
    on Ksiazka
    for each row
BEGIN
    IF NEW.Ilosc > 0 THEN
        SET NEW.Dostepnosc = 1; -- Ustaw dostępność na true
    ELSE
        SET NEW.Dostepnosc = 0; -- Ustaw dostępność na false
    END IF;
END;

create table Ksiazka_Jezyk
(
    ID_Ksiazki int null,
    ID_Jezyka  int null,
    constraint Ksiazka_Jezyk_Jezyk_ID_Jezyka_fk
        foreign key (ID_Jezyka) references Jezyk (ID_Jezyka),
    constraint Ksiazka_Jezyk_KSI___fk
        foreign key (ID_Ksiazki) references Ksiazka (ID_Ksiazki)
);

create table Recenzent
(
    ID_Wpisu    int auto_increment
        primary key,
    ID_Ksiazki  int         null,
    ID_Recenzji int         null,
    Recenzent   varchar(50) null,
    Firma       varchar(50) null,
    constraint recenzent_ksiazka
        foreign key (ID_Ksiazki) references Ksiazka (ID_Ksiazki),
    constraint recenzent_recenzja
        foreign key (ID_Recenzji) references Recenzja (ID_Recenzji)
);

create table Wypozyczenie
(
    ID_Wypozyczenia   int auto_increment
        primary key,
    ID_Ksiazki        int  null,
    ID_Czytelnika     int  null,
    ID_Pracownika     int  null,
    Data_Wypozyczenia date null,
    Data_Zwrotu       date null,
    constraint czyt_czyt
        foreign key (ID_Czytelnika) references Czytelnik (ID_Czytelnika),
    constraint ksi_ksi
        foreign key (ID_Ksiazki) references Ksiazka (ID_Ksiazki),
    constraint prac_prac
        foreign key (ID_Pracownika) references Pracownik (ID_Pracownika)
);

create definer = root@localhost trigger after_delete_wypozyczenie
    after delete
    on Wypozyczenie
    for each row
BEGIN
    -- Po usunięciu wypozyczenia zwiększ ilość dostępnych egzemplarzy książki
    UPDATE biblioteka.Ksiazka
    SET Ilosc = Ilosc + 1
    WHERE ID_Ksiazki = OLD.ID_Ksiazki;
END;

create definer = root@localhost trigger before_insert_check_max_books
    before insert
    on Wypozyczenie
    for each row
BEGIN
    DECLARE current_books_count INT;

    SELECT COUNT(*) INTO current_books_count
    FROM biblioteka.Wypozyczenie
    WHERE ID_Czytelnika = NEW.ID_Czytelnika;

    IF current_books_count >= 3 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Nie można wypożyczyć więcej niż 3 książki jednocześnie.';
    END IF;
END;

create definer = root@localhost trigger before_insert_check_max_books_update
    before update
    on Wypozyczenie
    for each row
BEGIN
    DECLARE current_books_count INT;

    SELECT COUNT(*) INTO current_books_count
    FROM biblioteka.Wypozyczenie
    WHERE ID_Czytelnika = NEW.ID_Czytelnika;

    IF current_books_count >= 3 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Nie można wypożyczyć więcej niż 3 książki jednocześnie.';
    END IF;
END;

create definer = root@localhost trigger before_insert_wypozyczenie
    before insert
    on Wypozyczenie
    for each row
BEGIN
    -- Przed wstawieniem wypozyczenia, sprawdź, czy ilość nie wynosi 0
    DECLARE ilosc_dostepnych INT;

    SELECT Ilosc INTO ilosc_dostepnych
    FROM biblioteka.Ksiazka
    WHERE ID_Ksiazki = NEW.ID_Ksiazki;

    IF ilosc_dostepnych > 0 THEN
        -- Jeśli ilość > 0, można wypożyczyć
        -- Zmniejsz ilość dostępnych egzemplarzy książki
        UPDATE biblioteka.Ksiazka
        SET Ilosc = Ilosc - 1
        WHERE ID_Ksiazki = NEW.ID_Ksiazki;
    ELSE
        -- W przeciwnym razie nie można wypożyczyć
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Nie można wypożyczyć. Brak dostępnych egzemplarzy.';
    END IF;
END;

create definer = root@localhost trigger wypozyczenia_na_dzien
    before insert
    on Wypozyczenie
    for each row
BEGIN
    DECLARE existing_rental_count INT;

    SELECT COUNT(*)
    INTO existing_rental_count
    FROM Wypozyczenie
    WHERE ID_Czytelnika = NEW.ID_Czytelnika
      AND ID_Ksiazki = NEW.ID_Ksiazki
      AND Data_Wypozyczenia >= (CURDATE() - INTERVAL 1 DAY);

    IF existing_rental_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Czytelnik nie może wypożyczyć dwóch egzemplarzy tej samej książki w krótkim odstępie czasu.';
    END IF;
END;

create
    definer = root@localhost procedure AddOrUpdateBook(IN p_Tytul varchar(255), IN p_ID_Autora int,
                                                       IN p_ID_Kategorii int, IN p_ID_Wydawnictwa int,
                                                       IN p_Rok_Wydania int, IN p_Ilosc int)
BEGIN
    DECLARE bookID INT;

    -- Sprawdź, czy książka już istnieje w bazie
    SELECT ID_Ksiazki INTO bookID
    FROM biblioteka.Ksiazka
    WHERE Tytul = p_Tytul;

    IF bookID IS NULL THEN
        -- Jeśli książka nie istnieje, dodaj ją do bazy
        INSERT INTO biblioteka.Ksiazka (Tytul, ID_Autora, ID_Kategorii, ID_Wydawnictwa, Rok_Wydania, Ilosc, Dostepnosc)
        VALUES (p_Tytul, p_ID_Autora, p_ID_Kategorii, p_ID_Wydawnictwa, p_Rok_Wydania, p_Ilosc, IF(p_Ilosc > 0, 1, 0));
    ELSE
        -- Jeśli książka istnieje, zaktualizuj jej ilość
        UPDATE biblioteka.Ksiazka
        SET Ilosc = Ilosc + p_Ilosc,
            Dostepnosc = IF((Ilosc + p_Ilosc) > 0, 1, 0)
        WHERE ID_Ksiazki = bookID;
    END IF;
END;

create
    definer = root@localhost procedure AddRental(IN p_ID_Ksiazki int, IN p_ID_Czytelnika int, IN p_ID_Pracownika int,
                                                 IN p_Data_Wypozyczenia date, IN p_Data_Zwrotu date)
BEGIN
    -- Dodaj nowe wypożyczenie
    INSERT INTO biblioteka.Wypozyczenie (ID_Ksiazki, ID_Czytelnika, ID_Pracownika, Data_Wypozyczenia, Data_Zwrotu)
    VALUES (p_ID_Ksiazki, p_ID_Czytelnika, p_ID_Pracownika, p_Data_Wypozyczenia, p_Data_Zwrotu);
END;

create
    definer = root@localhost procedure CancelRental(IN p_ID_Wypozyczenia int)
BEGIN
    -- Anuluj wypożyczenie
    DELETE FROM biblioteka.Wypozyczenie WHERE ID_Wypozyczenia = p_ID_Wypozyczenia;
END;

create
    definer = root@localhost procedure GetBookByTitle(IN bookTitle varchar(255))
BEGIN
    SELECT
        K.ID_Ksiazki,
        K.Tytul,
        A.Imie AS Autor_Imie,
        A.Nazwisko AS Autor_Nazwisko,
        K.Dostepnosc
    FROM
        Ksiazka K
        JOIN Autor A ON K.ID_Autora = A.ID_Autora
    WHERE
        K.Tytul = bookTitle;
END;

create
    definer = root@localhost procedure GetBookReport()
BEGIN
    SELECT
        K.ID_Ksiazki,
        K.Tytul,
        A.Imie AS Autor_Imie,
        A.Nazwisko AS Autor_Nazwisko,
        K.Rok_Wydania,
        K.Dostepnosc
    FROM
        Ksiazka K
        LEFT JOIN Autor A ON K.ID_Autora = A.ID_Autora;
END;

create
    definer = root@localhost procedure GetReviewsForBook(IN bookTitle varchar(255))
BEGIN
    SELECT
        K.Tytul AS Book_Title,
        R.Ocena AS Review_Rating,
        R.Opinia AS Review_Opinion,
        REC.Recenzent AS Reviewer_Name,
        REC.Firma AS Reviewer_Company
    FROM
        Ksiazka K
        JOIN Recenzent REC ON K.ID_Ksiazki = REC.ID_Ksiazki
        JOIN Recenzja R ON REC.ID_Recenzji = R.ID_Recenzji
    WHERE
        K.Tytul = bookTitle;
END;

create
    definer = root@localhost procedure IncreaseAllBooksAvailability(IN p_quantityToAdd int)
BEGIN
    DECLARE bookID INT;
    DECLARE done INT DEFAULT 0;

    DECLARE cur_books CURSOR FOR
    SELECT ID_Ksiazki
    FROM Ksiazka;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur_books;

    bookLoop: WHILE done = 0 DO
        FETCH cur_books INTO bookID;

        IF done = 1 THEN
            LEAVE bookLoop;
        END IF;

        UPDATE Ksiazka
        SET Ilosc = Ilosc + p_quantityToAdd
        WHERE ID_Ksiazki = bookID;
    END WHILE;

    CLOSE cur_books;
END;

create
    definer = root@localhost procedure WorkersRentalsInformation()
BEGIN
    SELECT
        P.ID_Pracownika,
        P.Imie,
        P.Nazwisko,
        COUNT(W.ID_Wypozyczenia) AS NumberOfRentals
    FROM
        biblioteka.Pracownik P
    LEFT JOIN
        biblioteka.Wypozyczenie W ON P.ID_Pracownika = W.ID_Pracownika
    GROUP BY
        P.ID_Pracownika, P.Imie, P.Nazwisko;
END;

