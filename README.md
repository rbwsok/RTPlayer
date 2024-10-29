# RTPlayer

[Скачать последнюю версию](https://github.com/rbwsok/RTPlayer/releases)

Музыкальный плеер для Chery Tiggo 7 Pro Max. Работает на нативном андроиде. Не через Android Auto!!! Устанавливается через adb. Можно устанавливать через ADB AppControl. Как получить доступ к adb - читаем на drive2.

Плеер будет работать и на других устройствах, но в данный момент затачивается только под экран и особенности Chery Tiggo. 

![Главное окно](https://github.com/rbwsok/RTPlayer/blob/main/doc/main.jpg)

### Возможности:
- проигрывание mp3 файлов
- поддержка штатных кнопок на руле
- обработка внешних событий, влияющих на звук (включение заднего ходаб уведомление от навигации и т.д.)
- регулировка пустого пространства (сжимание рабочей области) - чтобы от нажатий на краях экрана не вызывались системные окна (например управление климатом)
- встроенный FTP сервер для закачивания музыки с телефона или ПК (необходима активация WiFi в инженерном меню)
- внешний вид в стиле стандартных приложений (с возможностью переключения "только рабочая область"/"полный экран"). Фоновая картинка выдрана из прошивки.
- Регулировка размера текста на главном экране.
- Работа с двумя usb накопителями.

### Особенности штатной магнитолы и андроида Chery Tiggo 7 Pro Max (а так же 8 Pro Max и Arrizo 8)
- Android 9 (API Level 28)
- Андроид работает в виртуальной машине. Хостовя ось - QNX 7.
- Не очень быстрый процессор Renesas G6SH (2 Cortex A57 и 2 Cortex A53 в big-little)
- Полное разрешение 1920x720. Диагональ 12.3 дюйма. Рабочая область 1460x720.
- Очень неудобный штатный плеер
- Отсутствие кнопки "назад". Физической и виртуальной.
- Отсутствие стандартных настроек, возможности управления приложениями и их правами
- Нестандартные и очень странные размеры и положения тулбаров.
- Клик или свайп на границах экрана вызывает штатные окна.
  
### Причины написания своего плеера с блекждеком и куртизанками...

#### 1. Неудобный штатный плеер.

Штатный плеер музыки из папок на Tiggo 7 Pro Max выглядит красиво, но по функционалу очень слаб. В частности все mp3 файлы засовывает в один список. Также после старта надо совершить несколько щелчков для воспроизведения музыки. Никакой каталогизации или библиотеки.

#### 2. Неудобные или неработающие универсальные плееры.

Из за особенностей андроида в ГУ, на нем не работают (вылетают) многие плееры. Заработал AIMP. Но удобным и интуитивно понятным его не назовешь.

#### 3. Переключение юзер кейсов.

RTPlayer реализует два user case:
1. Проигрывание музыки по альбомам, композиция за композицией.
2. Случайное воспроизведение композиций из одной папки.

В частности - я люблю слушать по первому кейсу - один исполнитель, один альбом. А супруга такое не любит. Ей надо чтобы каждый раз была новая случайная песня. При чем репертуар пересекается примерно на 2/3. Из за этого моя музыка разложена по папкам, а ее - свалена в одну.

В режиме случайного воспроизведения, рулевые кнопки "вперед" и "назад" работают одинаково и переходят на случайную композицию.

#### 4. Выезжающие штатные окна.

Сжатие рабочей области позволяет в какой то мере решить эту проблему. Т.е. края экрана просто остаются пустыми. В RTPlayer сделана возможность регулировки пустого пространства со всех четырех сторон.

#### 5. FTP Сервер.

Позволяет закачивать музыку по воздуху. Не работает постоянно!!! Запускается только при ручном запуске пользователем.

Для работы нужен wifi. Обычно wifi настраивается на автоматическое подключение к точке доступа, например в телефоне. В этом случае с телефона становится доступен линк до плеера. Также можно подключить к той же точке доступа ПК и заходить в плеер с него.

Нужен любой FTP клиент. IP адреса выводятся на экран. Надо пробовать все. 

Можно попробовать пинговать. Если есть пинг - то и по ftp подключится.

Пароль - любое слово. Проверок нет.

Тестировался Total Commander для Windows и Total Commander c FTP плагином для Android. Если не подключается - то надо снять или поставить галку "пассивный режим". Положение этой настройки для TotalCommander для Windows и Android отличается.

Корневая папка для FTP - это "Папка с музыкой" в настройках плеера.

### Эргономика.

1. Плеер не должен отвлекать на себя внимание во время движения. Т.е. никаких обложек, анимации, эквалайзеров и прочего.
2. Плеер должен максимально напоминать штатный софт.
3. Плеер должен быть легким в управлении. Включая рулевые клавиши, а также "пианино" под экраном.
4. Чем меньше настроек, тем лучше.

### Тестирование

Прошивка 2.03 для 7ПМ.
Тестировалось в поездке на дальняк в паре с навигацией и в многочисленных поездках по городу.

### Установка и начало работы.

1. Установить через adb плеер.
2. Вставить флешку с музыкой.
3. Запустить плеер и в настройках в "Папка с музыкой" руками прописать путь. Для 7ПМ, usb накопители на нижней части консоли - это /storage/usb0 и /storage/usb1. Выбора навигацией по папкам нет!
4. Нажать на кнопку "Выход в корневую папку".
Если нужнен второй usb накопитель - то же самое сделать с настройкой "Дополнительная папка с музыкой".

### Кнопки на главном экране.

![Перемещение в корневую папку](https://github.com/rbwsok/RTPlayer/blob/main/doc/root.jpg) - Перемещение в корневую папку

![Перемещение в папку на уровень выше](https://github.com/rbwsok/RTPlayer/blob/main/doc/parent.jpg) - Перемещение в папку на уровень выше

![Настройки](https://github.com/rbwsok/RTPlayer/blob/main/doc/options.jpg) - Настройки

![Перемешивание](https://github.com/rbwsok/RTPlayer/blob/main/doc/shuffle.jpg) - Перемешивание

![Выход](https://github.com/rbwsok/RTPlayer/blob/main/doc/exit.jpg) - Выход (С остановкой процесса)

### TODO. Что еще предстоит сделать

- ~~Вынести в параллельный поток считывание информации о композициях. Сейчас может подтормаживать при резком скролле на больших количествах файлов.~~
- Сделать имитацию штатных софтовых кнопок.
- Доработать режим повторения "все звуки" т.к. сейчас работает криво. (Сейчас изображение есть, а клики не работаеют).
- Сделать автозагрузку. (скорее всего это невозможно на данном ГУ).
- Сделать ремаппер рулевых клавиш и "пианино" с подавлением штатных функций. (или сделать это сторонним софтом). Сейчас ремаппер НЕ работает. Есть только его настройка.
- Сделать подавление изображения штатного голосового помощника. (Не уверен что возможно со стороны софта без изменения глобальной конфигурации).
- Убрать переключение fullscreen отображения при изменении настроек.
- Сделать передачу информации о проигрываемом файле на приборную панель. (Понятия не имею как, но штатный плеер может)

### Сторонний код и ресурсы

Для реализации FTP была взята библиотека MinimalFTP - https://github.com/Guichaguri/MinimalFTP без каких либо изменений.

### Лицензия

MIT
