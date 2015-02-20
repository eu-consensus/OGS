-- phpMyAdmin SQL Dump
-- version 4.0.9
-- http://www.phpmyadmin.net
--
-- Φιλοξενητής: 127.0.0.1
-- Χρόνος δημιουργίας: 20 Φεβ 2015 στις 13:58:35
-- Έκδοση διακομιστή: 5.5.34
-- Έκδοση PHP: 5.4.22

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Βάση: `consensus_rm`
--

-- --------------------------------------------------------

--
-- Δομή πίνακα για τον πίνακα `erf_8_obj_game`
--

CREATE TABLE IF NOT EXISTS `erf_8_obj_game` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `P_ID` int(11) NOT NULL,
  `distance` double DEFAULT NULL,
  `dominatedbycategory` int(30) DEFAULT NULL,
  `dominatedbypool` int(30) DEFAULT NULL,
  `rank` int(30) DEFAULT NULL,
  `myorder` varchar(12) DEFAULT NULL,
  `chosen` int(30) DEFAULT '0',
  `liked` int(30) DEFAULT '0',
  `objscore` int(30) DEFAULT NULL,
  `prefscore` int(30) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `P_ID` (`P_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=41 ;

--
-- Άδειασμα δεδομένων του πίνακα `erf_8_obj_game`
--

INSERT INTO `erf_8_obj_game` (`ID`, `P_ID`, `distance`, `dominatedbycategory`, `dominatedbypool`, `rank`, `myorder`, `chosen`, `liked`, `objscore`, `prefscore`) VALUES
(1, 40, 6.0346, 0, 0, 1, '87534511', 0, 0, 500, 0),
(2, 38, 6.591, 0, 0, 1, '78436411', 0, 0, 500, 0),
(3, 36, 6.4265, 0, 0, 1, '87534511', 0, 0, 500, 0),
(4, 34, 6.7588, 0, 0, 1, '14587511', 0, 0, 500, 0),
(5, 28, 5.4333, 0, 0, 1, '67111181', 0, 0, 500, 0),
(6, 26, 6.0233, 0, 0, 1, '81547511', 0, 0, 500, 0),
(7, 24, 6.033, 0, 0, 1, '76325381', 0, 0, 500, 0),
(8, 22, 5.4333, 0, 0, 1, '67111181', 0, 0, 500, 0),
(9, 33, 6.7592, 0, 1, 2, '18476411', 0, 0, 465, 0),
(10, 32, 6.023, 0, 1, 2, '81645611', 0, 0, 465, 0),
(11, 30, 6.0328, 0, 1, 2, '76432481', 0, 0, 465, 0),
(12, 25, 6.0238, 0, 1, 2, '83547511', 0, 0, 465, 0),
(13, 23, 6.0343, 0, 1, 2, '67325381', 0, 0, 465, 0),
(14, 14, 6.7474, 0, 1, 2, '14685611', 0, 0, 465, 0),
(15, 39, 6.0359, 1, 1, 2, '87534511', 0, 0, 453, 0),
(16, 37, 6.5919, 1, 1, 2, '78436411', 0, 0, 453, 0),
(17, 35, 6.427, 1, 1, 2, '87534511', 0, 0, 453, 0),
(18, 27, 5.4355, 0, 2, 2, '67111171', 0, 0, 418, 0),
(19, 21, 5.4355, 0, 2, 2, '67111171', 0, 0, 418, 0),
(20, 6, 6.0104, 0, 2, 3, '81574511', 0, 0, 395, 0),
(21, 31, 6.0235, 0, 3, 3, '83645611', 0, 0, 360, 0),
(22, 29, 6.0341, 0, 3, 3, '67432481', 0, 0, 360, 0),
(23, 18, 6.5793, 0, 3, 3, '34587511', 0, 0, 360, 0),
(24, 16, 6.4145, 0, 3, 3, '43756711', 0, 0, 360, 0),
(25, 13, 6.7479, 1, 3, 3, '14685611', 0, 0, 348, 0),
(26, 12, 6.0104, 0, 3, 4, '41444411', 0, 0, 325, 0),
(27, 4, 6.0199, 0, 4, 3, '32756741', 0, 0, 290, 0),
(28, 5, 6.0109, 0, 5, 4, '83574511', 0, 0, 255, 0),
(29, 10, 6.0198, 0, 5, 4, '32685641', 0, 0, 255, 0),
(30, 17, 6.5801, 1, 5, 4, '34587511', 0, 0, 244, 0),
(31, 15, 6.4151, 1, 5, 4, '43756711', 0, 0, 244, 0),
(32, 3, 6.0213, 0, 7, 4, '23756741', 0, 0, 209, 0),
(33, 11, 6.0109, 0, 7, 5, '43444411', 0, 0, 186, 0),
(34, 9, 6.0212, 0, 9, 5, '23685641', 0, 0, 151, 0),
(35, 20, 6.0217, 0, 11, 4, '83574511', 0, 0, 116, 0),
(36, 8, 5.4182, 0, 12, 4, '23574581', 0, 0, 81, 0),
(37, 2, 5.4182, 0, 12, 4, '23574581', 0, 0, 81, 0),
(38, 19, 6.0231, 1, 13, 5, '83574511', 0, 0, 34, 0),
(39, 7, 5.4204, 0, 16, 5, '27463471', 0, 0, 0, 0),
(40, 1, 5.4204, 0, 16, 5, '27463471', 0, 0, 0, 0);

--
-- Περιορισμοί για άχρηστους πίνακες
--

--
-- Περιορισμοί για πίνακα `erf_8_obj_game`
--
ALTER TABLE `erf_8_obj_game`
  ADD CONSTRAINT `erf_8_obj_game_ibfk_1` FOREIGN KEY (`P_ID`) REFERENCES `erf_8_obj` (`ID`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
