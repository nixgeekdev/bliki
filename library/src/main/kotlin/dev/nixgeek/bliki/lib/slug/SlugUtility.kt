package dev.nixgeek.bliki.lib.slug

internal object SlugUtility {
    internal val PATTERN_IDENTIFY_SLUG = """^[a-z0-9]+(?:-[a-z0-9]+)*$""".toRegex()
    internal val PATTERN_NORMALIZE_HYPHEN_SEPARATOR = "[_\\W\\s+]+".toRegex()
    internal val PATTERN_NORMALIZE_NON_ASCII = "[^\\p{ASCII}]+".toRegex()
    internal val PATTERN_NORMALIZE_TRIM_DASH = "^-|-$".toRegex()

    internal val REPLACEMENTS: Map<Char, String> =
        buildMap {
            // Arabic
            put('\u0623', "a") // alef
            put('\u0628', "b") // beh
            put('\u062A', "t") // teh
            put('\u062B', "th") // theh
            put('\u062C', "g") // jeem
            put('\u062D', "h") // hah
            put('\u062E', "kh") // khah
            put('\u062F', "d") // dal
            put('\u0630', "th") // thal
            put('\u0631', "r") // reh
            put('\u0632', "z") // zain
            put('\u0633', "s") // seen
            put('\u0634', "sh") // sheen
            put('\u0635', "s") // sad
            put('\u0636', "d") // dad
            put('\u0637', "t") // tah
            put('\u0638', "th") // zah
            put('\u0639', "aa") // ain
            put('\u063A', "gh") // ghain
            put('\u0641', "f") // feh
            put('\u0642', "k") // qaf
            put('\u0643', "k") // kaf
            put('\u0644', "l") // lam
            put('\u0645', "m") // meem
            put('\u0646', "n") // noon
            put('\u0647', "h") // heh
            put('\u0648', "o") // waw
            put('\u064A', "y") // yeh

            // Cyrillic
            // uppercase
            put('\u0410', "A") // А A
            put('\u0411', "B") // Б Be
            put('\u0412', "V") // В Ve
            put('\u0413', "G") // Г Ghe
            put('\u0414', "D") // Д De
            put('\u0402', "Dj") // Ђ Dje
            put('\u0415', "E") // Е Ie
            put('\u0404', "E") // Є Ie Ukrainian
            put('\u0416', "Zh") // Ж Zhe
            put('\u0417', "Z") // З Ze
            put('\u0418', "I") // И I
            put('\u0408', "J") // Ј Je
            put('\u0419', "J") // Й Je
            put('\u041a', "K") // К Ka
            put('\u041b', "L") // Л El
            put('\u0409', "Lj") // Љ Lje
            put('\u041c', "M") // М Em
            put('\u041d', "N") // Н En
            put('\u040a', "Nj") // Њ Nje
            put('\u041e', "O") // О O
            put('\u041f', "P") // П Pe
            put('\u0420', "R") // Р Er
            put('\u0421', "S") // С Es
            put('\u0422', "T") // Т Te
            put('\u040b', "Tj") // Ћ Tshe
            put('\u0423', "U") // У U
            put('\u0424', "F") // Ф Ef
            put('\u0425', "H") // Х Ha
            put('\u0426', "Ts") // Ц Tse
            put('\u0427', "Ch") // Ч Che
            put('\u040f', "Dz") // Џ Dzhe
            put('\u0428', "Sh") // Ш Sha
            put('\u0429', "Shch") // Щ Shcha
            put('\u042a', "'") // Ъ
            put('\u042b', "Y") // Ы Yeru
            put('\u042c', "'") // Ь
            put('\u042d', "E") // Э E
            put('\u042e', "Yu") // Ю Yu
            put('\u042f', "Ya") // Я Ya
            // lowercase
            put('\u0430', "a") // а
            put('\u0431', "b") // б
            put('\u0432', "v") // в
            put('\u0433', "g") // г
            put('\u0434', "d") // д
            put('\u0452', "dj") // ђ
            put('\u0435', "e") // е
            put('\u0454', "e") // є
            put('\u0436', "zh") // ж
            put('\u0437', "z") // з
            put('\u0438', "i") // и
            put('\u0458', "j") // ј
            put('\u0439', "j") // й
            put('\u043a', "k") // к
            put('\u043b', "l") // л
            put('\u0459', "lj") // љ
            put('\u043c', "m") // м
            put('\u043d', "n") // н
            put('\u045a', "nj") // њ
            put('\u043e', "o") // о
            put('\u043f', "p") // п
            put('\u0440', "r") // р
            put('\u0441', "s") // с
            put('\u0442', "t") // т
            put('\u045b', "tj") // ћ
            put('\u0443', "u") // у
            put('\u0444', "f") // ф
            put('\u0445', "h") // х
            put('\u0446', "ts") // ц
            put('\u0447', "ch") // ч
            put('\u045f', "dz") // џ
            put('\u0448', "sh") // ш
            put('\u0449', "shch") // щ
            put('\u044a', "'") // ъ
            put('\u044b', "y") // ы
            put('\u044c', "'") // ь
            put('\u044d', "e") // э
            put('\u044e', "yu") // ю
            put('\u044f', "ya") // я

            // German
            put('\u00c4', "Ae") // Ä
            put('\u00e4', "ae") // ä
            put('\u00d6', "Oe") // Ö
            put('\u00f6', "oe") // ö
            put('\u00dc', "Ue") // Ü
            put('\u00fc', "ue") // ü
            put('\u00df', "ss") // ß eszett

            // Greek
            // uppercase
            put('\u0391', "A") // alpha
            put('\u0392', "B") // beta
            put('\u0393', "G") // gamma
            put('\u0394', "D") // delta
            put('\u0395', "E") // epsilon
            put('\u0396', "Z") // zeta
            put('\u0397', "H") // eta
            put('\u0398', "TH") // theta
            put('\u0399', "I") // iota
            put('\u039A', "K") // kappa
            put('\u039B', "L") // lambda
            put('\u039C', "M") // mu
            put('\u039D', "N") // nu
            put('\u039E', "KS") // xi
            put('\u039F', "O") // omicron
            put('\u03A0', "P") // pi
            put('\u03A1', "R") // rho
            put('\u03A3', "S") // sigma
            put('\u03A4', "T") // tau
            put('\u03A5', "Y") // upsilon
            put('\u03A6', "F") // phi
            put('\u03A7', "X") // chi
            put('\u03A8', "PS") // psi
            put('\u03A9', "W") // omega
            // lowercase
            put('\u03B1', "a")
            put('\u03B2', "b")
            put('\u03B3', "g")
            put('\u03B4', "d")
            put('\u03B5', "e")
            put('\u03B6', "z")
            put('\u03B7', "h")
            put('\u03B8', "th")
            put('\u03B9', "i")
            put('\u03BA', "k")
            put('\u03BB', "l")
            put('\u03BC', "m")
            put('\u03BD', "n")
            put('\u03BE', "ks")
            put('\u03BF', "o")
            put('\u03C0', "p")
            put('\u03C1', "r")
            put('\u03C2', "s")
            put('\u03C3', "s")
            put('\u03C4', "t")
            put('\u03C5', "y")
            put('\u03C6', "f")
            put('\u03C7', "x")
            put('\u03C8', "ps")
            put('\u03C9', "w")
            // tonos
            put('\u0386', "A") // Ά
            put('\u0388', "E") // Έ
            put('\u0389', "H") // Ή
            put('\u038A', "I") // Ί
            put('\u038C', "O") // Ό
            put('\u038E', "Y") // Ύ
            put('\u038F', "W") // Ώ
            put('\u0390', "i") // ΐ
            put('\u03AA', "I") // Ϊ
            put('\u03AB', "Y") // Ϋ
            put('\u03AC', "a") // ά
            put('\u03AD', "e") // έ
            put('\u03AE', "h") // ή
            put('\u03AF', "i") // ί
            put('\u03B0', "y") // ΰ
            put('\u03CA', "i") // ϊ
            put('\u03CB', "y") // ϋ
            put('\u03CC', "o") // ό
            put('\u03CD', "y") // ύ
            put('\u03CE', "w") // ώ
            put('\u03CF', "and") // Ϗ
            put('\u03D7', "and") // ϗ

            // Nordic
            put('\u00e5', "aa") // å angstrom
            put('\u00c5', "Aa") // Å
            put('\u00e6', "ae") // æ aeye
            put('\u00c6', "Ae") // Æ
            put('\u00f8', "oe") // ø rune-o
            put('\u00d8', "Oe") // Ø
            put('\u0153', "oe") // œ oethel
            put('\u0152', "Oe") // Œ
            put('\u00f0', "th") // ð eth
            put('\u00d0', "Th") // Ð
            put('\u00fe', "th") // þ thorn
            put('\u00de', "Th") // Þ

            // Polish
            put('\u0141', "L") // Ł
            put('\u0142', "l") // ł

            // Turkish
            put('\u011e', "g") // Ğ
            put('\u011f', "g") // ğ
            put('\u0130', "i") // İ
            put('\u0131', "i") // ı
            put('\u015e', "s") // Ş
            put('\u015f', "s") // ş
        }
}
