# KeepNotes for Nextcloud (Android Client)

[![License: Apache-2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg)](https://kotlinlang.org)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-28-green.svg)](https://developer.android.com)

[English](#english) | [Español](#español) | [Português](#português) | [Français](#français) | [Deutsch](#deutsch) | [Русский](#русский)

---

<a name="русский"></a>
<a name="russian"></a>
## 🇷🇺 Русский

**KeepNotes for Nextcloud** — быстрый, открытый Android-клиент для [Nextcloud Notes](https://apps.nextcloud.com/apps/notes) с интерфейсом и пользовательским опытом в стиле **Google Keep** (адаптивная сетка заметок, тема Material 3, интерактивные чек-листы, категории, полнофункциональный офлайн-режим с двусторонней синхронизацией).

Приложение разработано без закрытых библиотек и трекеров, не использует Google Play Services и полностью соответствует стандартам свободного ПО (F-Droid).

### ✨ Основные возможности

- **Интерфейс в стиле Google Keep**: адаптивная сетка заметок (Staggered Grid) с поддержкой динамических цветов Material 3 (светлая и тёмная темы).
- **Бесшовная авторизация**: поддержка официального **Nextcloud Login Flow v2** (вход в один клик через браузер без ручного ввода паролей приложений).
- **Полноценный Offline-First**:
  - Локальная база данных Room выступает единым источником правды (*Single Source of Truth*).
  - Мгновенное создание, редактирование и удаление заметок без ожидания ответа сервера.
  - Двусторонняя синхронизация с разрешением конфликтов по `etag` и временным меткам.
  - Фоновая периодическая синхронизация через **WorkManager**, ручное обновление жестом *Pull-to-Refresh* и тонкая полоса индикации прогресса в верхней панели.
- **Продвинутый Markdown и списки задач**:
  - **Режим редактирования (Edit)**: умное автодополнение списков при нажатии Enter (с сохранением отступов), быстрый выход из списка, автоматическая очистка пустых пунктов.
  - **Режим просмотра (Preview)**: крупные кликабельные чекбоксы в стиле Google Keep с автоматическим зачёркиванием выполненных пунктов (`~~текст~~`).
  - Быстрая конвертация выделенного или всего текста в список и обратно («Сделать список» / «Обычный текст»).
  - Корректная обработка переносов строк (*Soft line breaks*) и автоматическая дедупликация заголовков, созданных в веб-клиенте.
- **Организация и навигация**:
  - Навигационная шторка (Navigation Drawer) со списком всех категорий и счётчиками заметок.
  - Удобный выбор и создание категорий через BottomSheet.
  - Избранные заметки (Star / Favorite) и быстрый поиск по содержимому и заголовкам.
- **Безопасность и экран настроек**:
  - **Биометрическая защита (App Lock)**: блокировка приложения по отпечатку пальца, лицу или системному PIN-коду устройства (AndroidX Biometric, 100% F-Droid compliant).
  - **Размеры шрифтов**: независимый выбор размера шрифта для карточек и редактора (Маленький, Обычный, Крупный, Очень крупный).
  - **Управление подтверждениями**: включение/отключение диалогов подтверждения сохранения при выходе и удаления заметок.
- **Мультиязычность**:
  - Встроенный выбор языка (русский, английский, испанский, португальский, французский, немецкий) прямо в настройках приложения (Per-App Language).
- **Приватность**:
  - 100% открытый исходный код без Google Play Services, рекламы, телеметрии и аналитики.

### 🛠 Технологический стек

| Слой | Технологии |
|---|---|
| Язык | Kotlin 2.1.0 |
| UI | XML Views + RecyclerView (StaggeredGridLayoutManager) + Material 3 Components |
| Архитектура | MVVM (ViewModel + StateFlow) + Single Activity |
| Локальная БД | Room 2.7.1 (SQLite) |
| Сеть | Retrofit 2.11.0 + OkHttp 4.12.0 + Nextcloud Notes REST API v1 |
| Внедрение зависимостей | Dagger Hilt 2.56.2 |
| Рендеринг Markdown | Markwon 4.6.2 (Custom TaskListPlugin, SoftLineBreak, Strikethrough, Tables) |
| Безопасность | AndroidX Biometric 1.1.0 + EncryptedSharedPreferences |
| Фоновые задачи | AndroidX WorkManager |
| Мин. / Целевая версия SDK | Min SDK 28 (Android 9.0) / Target SDK 35 (Android 15) |

### 🚀 Сборка и запуск

1. **Клонирование репозитория**:
   ```bash
   git clone https://github.com/maelst-dev/KeepNotes-for-Nextcloud.git
   ```

2. **Открытие в Android Studio**:
   - Запустите Android Studio (Ladybug / Meerkat или новее).
   - Выберите **File ➔ Open** и укажите папку репозитория.
   - Дождитесь завершения автоматической синхронизации Gradle (*Gradle Sync*).

3. **Запуск**:
   - Подключите реальное Android-устройство или запустите эмулятор (API 28+).
   - Нажмите кнопку **Run** (`Shift + F10`).

### 📄 Лицензия

Проект распространяется под свободной лицензией **Apache License 2.0**. Полный текст доступен в файле [LICENSE](LICENSE).

### ⚖️ Отказ от ответственности

KeepNotes является независимым приложением и не связано с Nextcloud GmbH, не спонсируется и не поддерживается им. Nextcloud является зарегистрированным товарным знаком Nextcloud GmbH.

---

<a name="english"></a>
## 🇬🇧 English

**KeepNotes for Nextcloud** is a fast, open-source Android client for [Nextcloud Notes](https://apps.nextcloud.com/apps/notes) featuring a **Google Keep-inspired** user experience (staggered card grid, Material 3 dynamic theming, interactive checklists, categories, offline-first bidirectional sync).

Built strictly without proprietary blobs or tracking libraries, free from Google Play Services, and fully compliant with F-Droid inclusion standards.

### ✨ Features

- **Google Keep-inspired UI**: Adaptive staggered grid card layout with Material 3 dynamic color theming (Light & Dark modes).
- **Seamless Authentication**: Nextcloud **Login Flow v2** support (1-click browser login without manual app password setup).
- **Robust Offline-First Architecture**:
  - Local Room database acts as the single source of truth.
  - Instant local operations (create, update, delete) without waiting for network responses.
  - Bidirectional server synchronization using `etag` and `modified` timestamps.
  - Background periodic sync powered by **WorkManager**, manual Pull-to-Refresh, and top progress bar indicator.
- **Rich Markdown & Interactive Checklists**:
  - **Edit Mode**: Smart Enter key handling that auto-inserts new checklist items (preserving indentation) and exits cleanly on empty items.
  - **Preview Mode**: Large Google Keep-style clickable checkboxes with automatic strikethrough (`~~text~~`) on completed items.
  - 1-tap conversion: Plain text ➔ Checklist and Checklist ➔ Plain text.
  - Unified soft line break handling and automatic title deduplication for notes created in Nextcloud web.
- **Organization & Navigation**:
  - Navigation Drawer with categories and note counts.
  - Category Picker BottomSheet to assign or create categories on the fly.
  - Starred / Favorites filter and quick search across titles and content.
- **Security & User Preferences**:
  - **Biometric App Lock**: Secure your notes with fingerprint, face recognition, or device PIN/pattern (AndroidX Biometric, 100% F-Droid compliant).
  - **Font Size Presets**: Configurable font sizes for cards and editor (Small, Medium, Large, Extra Large).
  - **Confirmation Dialogs**: Toggleable save-on-exit and note deletion confirmations.
- **In-App Language Selection**:
  - Change application language (English, Spanish, Portuguese, French, German, Russian) directly from the settings screen.
- **Privacy First**:
  - 100% Free & Open Source software with zero tracking, zero analytics, zero ads, and no Google Play Services dependencies.

### 🛠 Tech Stack

| Layer | Tech |
|---|---|
| Language | Kotlin 2.1.0 |
| UI | XML Views + RecyclerView (StaggeredGridLayoutManager) + Material 3 Components |
| Architecture | MVVM (ViewModel + StateFlow) + Single Activity |
| Local Storage | Room 2.7.1 (SQLite) |
| Networking | Retrofit 2.11.0 + OkHttp 4.12.0 + Nextcloud Notes REST API v1 |
| Dependency Injection | Dagger Hilt 2.56.2 |
| Markdown Engine | Markwon 4.6.2 (Custom TaskListPlugin, SoftLineBreak, Strikethrough, Tables) |
| Security | AndroidX Biometric 1.1.0 + EncryptedSharedPreferences |
| Background Work | AndroidX WorkManager |
| Min / Target SDK | Min SDK 28 (Android 9.0) / Target SDK 35 (Android 15) |

### 🚀 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/maelst-dev/KeepNotes-for-Nextcloud.git
   ```

2. **Open in Android Studio**:
   - Open Android Studio (Ladybug / Meerkat or newer).
   - Select **File ➔ Open** and choose the cloned repository folder.
   - Wait for the initial Gradle sync to complete.

3. **Build & Run**:
   - Connect an Android device or start an emulator (API 28+).
   - Click **Run** (`Shift + F10`).

### 📄 License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](LICENSE) file for details.

### ⚖️ Disclaimer

KeepNotes is an independent application and is not affiliated with, sponsored, or endorsed by Nextcloud GmbH. Nextcloud is a registered trademark of Nextcloud GmbH.

---

<a name="español"></a>
<a name="spanish"></a>
## 🇪🇸 Español

**KeepNotes for Nextcloud** es un cliente de Android rápido y de código abierto para [Nextcloud Notes](https://apps.nextcloud.com/apps/notes), con una experiencia de usuario inspirada en **Google Keep** (cuadrícula de notas escalonada, tema dinámico Material 3, listas de tareas interactivas, categorías y sincronización bidireccional offline-first).

Desarrollado sin bibliotecas propietarias ni rastreadores, no utiliza Google Play Services y cumple plenamente con los estándares de inclusión de software libre (F-Droid).

### ✨ Características

- **Interfaz inspirada en Google Keep**: diseño de cuadrícula escalonada (Staggered Grid) con soporte para colores dinámicos de Material 3 (temas claro y oscuro).
- **Autenticación fluida**: soporte oficial para **Nextcloud Login Flow v2** (inicio de sesión en un clic a través del navegador sin configurar manualmente contraseñas de aplicación).
- **Arquitectura Offline-First robusta**:
  - La base de datos local Room actúa como única fuente de verdad (*Single Source of Truth*).
  - Creación, edición y eliminación instantáneas de notas sin esperar la respuesta del servidor.
  - Sincronización bidireccional con el servidor mediante `etag` y marcas de tiempo (`modified`).
  - Sincronización periódica en segundo plano mediante **WorkManager**, actualización manual arrastrando hacia abajo (*Pull-to-Refresh*) y una sutil barra de progreso en la parte superior.
- **Markdown avanzado y listas de tareas interactivas**:
  - **Modo de edición (Edit)**: inserción inteligente de nuevos elementos de lista al presionar Enter (conservando la sangría), salida rápida de la lista y limpieza automática de elementos vacíos.
  - **Modo de vista previa (Preview)**: casillas de verificación grandes y táctiles al estilo Google Keep con tachado automático de los elementos completados (`~~texto~~`).
  - Conversión con 1 toque: Texto plano ➔ Lista de tareas y Lista de tareas ➔ Texto plano.
  - Manejo unificado de saltos de línea suaves (*Soft line breaks*) y deduplicación automática de títulos para notas creadas en el cliente web de Nextcloud.
- **Organización y navegación**:
  - Menú lateral de navegación (Navigation Drawer) con todas las categorías y contadores de notas.
  - Selector de categorías mediante BottomSheet para asignar o crear categorías al vuelo.
  - Filtro de notas destacadas / favoritas y búsqueda rápida por título y contenido.
- **Seguridad y preferencias de usuario**:
  - **Bloqueo biométrico (App Lock)**: protege tus notas con huella dactilar, reconocimiento facial o PIN/patrón del dispositivo (AndroidX Biometric, 100% compatible con F-Droid).
  - **Ajustes de tamaño de fuente**: tamaños de fuente independientes para tarjetas y editor (Pequeño, Mediano, Grande, Extra grande).
  - **Diálogos de confirmación**: activación/desactivación de advertencias al guardar al salir y al eliminar notas.
- **Selección de idioma en la aplicación**:
  - Cambio de idioma de la aplicación (español, inglés, portugués, francés, alemán, ruso) directamente desde los ajustes.
- **Privacidad ante todo**:
  - 100% software libre y de código abierto, sin rastreo, sin telemetría, sin anuncios y sin dependencias de Google Play Services.

### 🛠 Pila tecnológica

| Capa | Tecnologías |
|---|---|
| Lenguaje | Kotlin 2.1.0 |
| Interfaz de usuario | XML Views + RecyclerView (StaggeredGridLayoutManager) + Componentes de Material 3 |
| Arquitectura | MVVM (ViewModel + StateFlow) + Single Activity |
| Almacenamiento local | Room 2.7.1 (SQLite) |
| Red | Retrofit 2.11.0 + OkHttp 4.12.0 + Nextcloud Notes REST API v1 |
| Inyección de dependencias | Dagger Hilt 2.56.2 |
| Motor de Markdown | Markwon 4.6.2 (Custom TaskListPlugin, SoftLineBreak, Strikethrough, Tables) |
| Seguridad | AndroidX Biometric 1.1.0 + EncryptedSharedPreferences |
| Tareas en segundo plano | AndroidX WorkManager |
| SDK Mín. / Objetivo | Min SDK 28 (Android 9.0) / Target SDK 35 (Android 15) |

### 🚀 Primeros pasos

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/maelst-dev/KeepNotes-for-Nextcloud.git
   ```

2. **Abrir en Android Studio**:
   - Inicie Android Studio (Ladybug / Meerkat o superior).
   - Seleccione **File ➔ Open** y elija la carpeta del repositorio clonado.
   - Espere a que finalice la sincronización inicial de Gradle (*Gradle Sync*).

3. **Compilar y ejecutar**:
   - Conecte un dispositivo Android físico o inicie un emulador (API 28+).
   - Haga clic en **Run** (`Shift + F10`).

### 📄 Licencia

Este proyecto está licenciado bajo la **Apache License 2.0**; consulte el archivo [LICENSE](LICENSE) para más detalles.

### ⚖️ Descargo de responsabilidad

KeepNotes es una aplicación independiente y no está afiliada, patrocinada ni respaldada por Nextcloud GmbH. Nextcloud es una marca registrada de Nextcloud GmbH.

---

<a name="português"></a>
<a name="portuguese"></a>
## 🇵🇹 Português

O **KeepNotes for Nextcloud** é um cliente Android rápido e de código aberto para o [Nextcloud Notes](https://apps.nextcloud.com/apps/notes), com uma experiência de utilizador inspirada no **Google Keep** (grelha escalonada de notas, tema dinâmico Material 3, listas de verificação interativas, categorias e sincronização bidirecional offline-first).

Desenvolvido sem bibliotecas proprietárias nem rastreadores, não utiliza o Google Play Services e está em total conformidade com os padrões de inclusão do F-Droid.

### ✨ Funcionalidades

- **Interface inspirada no Google Keep**: esquema em grelha escalonada (Staggered Grid) com suporte para cores dinâmicas do Material 3 (temas claro e escuro).
- **Autenticação simples**: suporte oficial ao **Nextcloud Login Flow v2** (início de sessão com um clique através do navegador, sem necessidade de senhas de aplicação manuais).
- **Arquitetura Offline-First robusta**:
  - A base de dados local Room atua como fonte única da verdade (*Single Source of Truth*).
  - Criação, edição e eliminação instantâneas de notas sem aguardar respostas de rede.
  - Sincronização bidirecional com o servidor usando `etag` e carimbos de data/hora (`modified`).
  - Sincronização periódica em segundo plano através do **WorkManager**, atualização manual puxando para baixo (*Pull-to-Refresh*) e barra de progresso no topo da janela.
- **Markdown avançado e listas de verificação interativas**:
  - **Modo de edição (Edit)**: inserção inteligente de novos itens na lista ao premir Enter (preservando o recuo) e saída limpa em itens vazios.
  - **Modo de visualização (Preview)**: caixas de seleção grandes no estilo Google Keep com riscado automático nos itens concluídos (`~~texto~~`).
  - Conversão com 1 toque: Texto simples ➔ Lista de verificação e Lista de verificação ➔ Texto simples.
  - Gestão consistente de quebras de linha suaves (*Soft line breaks*) e desduplicação automática de títulos para notas criadas na versão web do Nextcloud.
- **Organização e navegação**:
  - Painel de navegação lateral (Navigation Drawer) com todas as categorias e contadores de notas.
  - Seletor de categorias em BottomSheet para atribuir ou criar categorias rapidamente.
  - Filtro de notas favoritas/destacadas e pesquisa rápida por títulos e conteúdo.
- **Segurança e preferências do utilizador**:
  - **Bloqueio biométrico (App Lock)**: proteja as suas notas com impressão digital, reconhecimento facial ou PIN/padrão do dispositivo (AndroidX Biometric, 100% compatível com F-Droid).
  - **Ajuste do tamanho de letra**: definições independentes de tamanho de fonte para os cartões e o editor (Pequeno, Médio, Grande, Extra grande).
  - **Caixas de diálogo de confirmação**: ativação/desativação de confirmações ao guardar ao sair e ao eliminar notas.
- **Seleção de idioma na aplicação**:
  - Altere o idioma da aplicação (português, inglês, espanhol, francês, alemão, russo) diretamente nas definições.
- **Privacidade em primeiro lugar**:
  - 100% software livre e de código aberto, sem rastreamento, sem telemetria, sem anúncios e sem dependências do Google Play Services.

### 🛠 Tecnologias

| Camada | Tecnologias |
|---|---|
| Linguagem | Kotlin 2.1.0 |
| Interface (UI) | XML Views + RecyclerView (StaggeredGridLayoutManager) + Componentes Material 3 |
| Arquitetura | MVVM (ViewModel + StateFlow) + Single Activity |
| Armazenamento local | Room 2.7.1 (SQLite) |
| Rede | Retrofit 2.11.0 + OkHttp 4.12.0 + Nextcloud Notes REST API v1 |
| Injeção de dependências | Dagger Hilt 2.56.2 |
| Motor de Markdown | Markwon 4.6.2 (Custom TaskListPlugin, SoftLineBreak, Strikethrough, Tables) |
| Segurança | AndroidX Biometric 1.1.0 + EncryptedSharedPreferences |
| Tarefas em segundo plano | AndroidX WorkManager |
| SDK Mín. / Alvo | Min SDK 28 (Android 9.0) / Target SDK 35 (Android 15) |

### 🚀 Primeiros passos

1. **Clonar o repositório**:
   ```bash
   git clone https://github.com/maelst-dev/KeepNotes-for-Nextcloud.git
   ```

2. **Abrir no Android Studio**:
   - Abra o Android Studio (Ladybug / Meerkat ou mais recente).
   - Selecione **File ➔ Open** e aponte para a pasta do repositório clonado.
   - Aguarde até que a sincronização do Gradle (*Gradle Sync*) seja concluída.

3. **Compilar e executar**:
   - Ligue um dispositivo Android físico ou inicie um emulador (API 28+).
   - Clique em **Run** (`Shift + F10`).

### 📄 Licença

Este projeto está licenciado sob a **Apache License 2.0** — consulte o ficheiro [LICENSE](LICENSE) para obter detalhes.

### ⚖️ Aviso legal

O KeepNotes é uma aplicação independente e não é afiliada, patrocinada nem recomendada pela Nextcloud GmbH. Nextcloud é uma marca registada da Nextcloud GmbH.

---

<a name="français"></a>
<a name="french"></a>
## 🇫🇷 Français

**KeepNotes for Nextcloud** est un client Android rapide et open source pour [Nextcloud Notes](https://apps.nextcloud.com/apps/notes), offrant une expérience utilisateur inspirée de **Google Keep** (grille de notes en mosaïque, thème dynamique Material 3, listes de tâches interactives, catégories et synchronisation bidirectionnelle offline-first).

Développée sans bibliothèques propriétaires ni traqueurs, l'application n'utilise pas les services Google Play et respecte pleinement les critères d'inclusion de F-Droid.

### ✨ Fonctionnalités

- **Interface inspirée de Google Keep** : disposition en grille décalée (Staggered Grid) avec prise en charge des couleurs dynamiques de Material 3 (thèmes clair et sombre).
- **Authentification fluide** : prise en charge officielle du **Nextcloud Login Flow v2** (connexion en un clic via le navigateur sans saisie manuelle de mot de passe d'application).
- **Architecture Offline-First robuste** :
  - La base de données locale Room fait office de source unique de vérité (*Single Source of Truth*).
  - Création, modification et suppression instantanées des notes sans attendre la réponse du serveur.
  - Synchronisation bidirectionnelle avec le serveur via `etag` et horodatages (`modified`).
  - Synchronisation périodique en arrière-plan avec **WorkManager**, rafraîchissement manuel par glissement vers le bas (*Pull-to-Refresh*) et fine barre de progression dans la barre d'outils supérieure.
- **Markdown enrichi et listes de tâches interactives** :
  - **Mode édition (Edit)** : ajout intelligent de nouveaux éléments de liste lors de l'appui sur Entrée (en préservant l'indentation), sortie rapide de la liste et nettoyage automatique des lignes vides.
  - **Mode aperçu (Preview)** : grandes cases à cocher tactiles au style Google Keep avec texte barré automatique sur les éléments terminés (`~~texte~~`).
  - Conversion en 1 geste : Texte brut ➔ Liste de tâches et Liste de tâches ➔ Texte brut.
  - Gestion unifiée des retours à la ligne souples (*Soft line breaks*) et déduplication automatique des titres créés sur l'interface web de Nextcloud.
- **Organisation et navigation** :
  - Volet de navigation latéral (Navigation Drawer) avec liste de toutes les catégories et compteurs de notes.
  - Sélecteur de catégorie en BottomSheet pour attribuer ou créer des catégories instantanément.
  - Filtre des notes favorites (Star / Favorite) et recherche rapide dans les titres et le contenu.
- **Sécurité et préférences utilisateur** :
  - **Verrouillage biométrique (App Lock)** : sécurisez vos notes à l'aide de votre empreinte digitale, reconnaissance faciale ou code PIN/schéma de l'appareil (AndroidX Biometric, 100 % conforme F-Droid).
  - **Tailles de police personnalisables** : choix indépendant de la taille de police pour les cartes et l'éditeur (Petit, Moyen, Grand, Très grand).
  - **Boîtes de dialogue de confirmation** : activation/désactivation de la confirmation d'enregistrement en quittant et de la suppression de notes.
- **Choix de la langue dans l'application** :
  - Changement de la langue de l'application (français, anglais, espagnol, portugais, allemand, russe) directement dans les paramètres.
- **Respect absolu de la vie privée** :
  - 100 % logiciel libre et open source, sans aucun traqueur, sans analytique, sans publicité et sans dépendance aux services Google Play.

### 🛠 Technologies

| Couche | Technologies |
|---|---|
| Langage | Kotlin 2.1.0 |
| Interface utilisateur | XML Views + RecyclerView (StaggeredGridLayoutManager) + Composants Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) + Single Activity |
| Stockage local | Room 2.7.1 (SQLite) |
| Réseau | Retrofit 2.11.0 + OkHttp 4.12.0 + Nextcloud Notes REST API v1 |
| Injection de dépendances | Dagger Hilt 2.56.2 |
| Moteur Markdown | Markwon 4.6.2 (Custom TaskListPlugin, SoftLineBreak, Strikethrough, Tables) |
| Sécurité | AndroidX Biometric 1.1.0 + EncryptedSharedPreferences |
| Tâches en arrière-plan | AndroidX WorkManager |
| SDK Min. / Cible | Min SDK 28 (Android 9.0) / Target SDK 35 (Android 15) |

### 🚀 Prise en main

1. **Cloner le dépôt** :
   ```bash
   git clone https://github.com/maelst-dev/KeepNotes-for-Nextcloud.git
   ```

2. **Ouvrir dans Android Studio** :
   - Lancez Android Studio (Ladybug / Meerkat ou plus récent).
   - Sélectionnez **File ➔ Open** et choisissez le dossier du dépôt.
   - Attendez la fin de la synchronisation initiale de Gradle (*Gradle Sync*).

3. **Compiler et exécuter** :
   - Connectez un appareil Android physique ou démarrez un émulateur (API 28+).
   - Cliquez sur **Run** (`Shift + F10`).

### 📄 Licence

Ce projet est sous licence **Apache License 2.0** — consultez le fichier [LICENSE](LICENSE) pour plus d'informations.

### ⚖️ Avis de non-responsabilité

KeepNotes est une application indépendante qui n'est ni affiliée, ni sponsorisée, ni approuvée par Nextcloud GmbH. Nextcloud est une marque déposée de Nextcloud GmbH.

---

<a name="deutsch"></a>
<a name="german"></a>
## 🇩🇪 Deutsch

**KeepNotes for Nextcloud** ist ein schneller, quelloffener Android-Client für [Nextcloud Notes](https://apps.nextcloud.com/apps/notes), der eine an **Google Keep** angelehnte Benutzeroberfläche bietet (versetztes Notizraster, dynamisches Material-3-Design, interaktive Checklisten, Kategorien und bidirektionale Offline-First-Synchronisation).

Entwickelt ohne proprietäre Bibliotheken oder Tracker, kommt die App vollständig ohne Google Play Services aus und erfüllt sämtliche Kriterien für F-Droid.

### ✨ Funktionen

- **Von Google Keep inspirierte Oberfläche**: Adaptives, versetztes Kartenraster (Staggered Grid) mit Unterstützung für dynamische Material-3-Farben (Helles und Dunkles Design).
- **Nahtlose Authentifizierung**: Offizielle Unterstützung für **Nextcloud Login Flow v2** (1-Klick-Anmeldung über den Browser ohne manuelle Eingabe von App-Passwörtern).
- **Robuste Offline-First-Architektur**:
  - Lokale Room-Datenbank als zentrale Datenquelle (*Single Source of Truth*).
  - Sofortiges Erstellen, Bearbeiten und Löschen von Notizen ohne Warten auf Netzwerkantworten.
  - Bidirektionale Serversynchronisation über `etag` und Zeitstempel (`modified`).
  - Regelmäßige Hintergrundsynchronisation über **WorkManager**, manuelles Aktualisieren per Wischgeste (*Pull-to-Refresh*) und dezente Fortschrittsanzeige in der oberen Leiste.
- **Erweitertes Markdown und interaktive Checklisten**:
  - **Bearbeitungsmodus (Edit)**: Intelligente Eingabetasten-Behandlung, die automatisch neue Listenelemente einfügt (Einrückung bleibt erhalten) und leere Einträge sauber entfernt.
  - **Vorschaumodus (Preview)**: Große, antippbare Kontrollkästchen im Google-Keep-Stil mit automatischer Durchstreichung erledigter Einträge (`~~Text~~`).
  - 1-Klick-Konvertierung: Fließtext ➔ Checkliste und Checkliste ➔ Fließtext.
  - Einheitliche Behandlung weicher Zeilenumbrüche (*Soft line breaks*) und automatische Deduplizierung von Titeln, die im Nextcloud-Webclient erstellt wurden.
- **Organisation und Navigation**:
  - Seitliches Navigationsmenü (Navigation Drawer) mit allen Kategorien und Notizenzählern.
  - Komfortable Kategorieauswahl via BottomSheet zum schnellen Zuweisen oder Erstellen von Kategorien.
  - Favoriten-Filter (Star / Favorite) und schnelle Volltextsuche in Titeln und Notizinhalten.
- **Sicherheit und Benutzereinstellungen**:
  - **Biometrische App-Sperre (App Lock)**: Schütze deine Notizen mit Fingerabdruck, Gesichtserkennung oder Geräte-PIN/Muster (AndroidX Biometric, 100 % F-Droid-konform).
  - **Schriftgrößen-Voreinstellungen**: Unabhängig voneinander anpassbare Schriftgrößen für Karten und Editor (Klein, Mittel, Groß, Sehr groß).
  - **Bestätigungsdialoge**: Ein-/Ausschalten von Abfragen beim Speichern beim Verlassen und beim Löschen von Notizen.
- **In-App-Sprachauswahl**:
  - Wechseln der App-Sprache (Deutsch, Englisch, Spanisch, Portugiesisch, Französisch, Russisch) direkt in den App-Einstellungen.
- **Datenschutz an erster Stelle**:
  - 100 % freie und Open-Source-Software ohne Tracking, ohne Analysedienste, werbefrei und ohne Abhängigkeiten von Google Play Services.

### 🛠 Tech-Stack

| Ebene | Technologien |
|---|---|
| Sprache | Kotlin 2.1.0 |
| Benutzeroberfläche | XML Views + RecyclerView (StaggeredGridLayoutManager) + Material 3 Components |
| Architektur | MVVM (ViewModel + StateFlow) + Single Activity |
| Lokale Datenbank | Room 2.7.1 (SQLite) |
| Netzwerk | Retrofit 2.11.0 + OkHttp 4.12.0 + Nextcloud Notes REST API v1 |
| Dependency Injection | Dagger Hilt 2.56.2 |
| Markdown-Engine | Markwon 4.6.2 (Custom TaskListPlugin, SoftLineBreak, Strikethrough, Tables) |
| Sicherheit | AndroidX Biometric 1.1.0 + EncryptedSharedPreferences |
| Hintergrundaufgaben | AndroidX WorkManager |
| Min / Ziel-SDK | Min SDK 28 (Android 9.0) / Target SDK 35 (Android 15) |

### 🚀 Erste Schritte

1. **Repository klonen**:
   ```bash
   git clone https://github.com/maelst-dev/KeepNotes-for-Nextcloud.git
   ```

2. **In Android Studio öffnen**:
   - Starte Android Studio (Ladybug / Meerkat oder neuer).
   - Wähle **File ➔ Open** und wähle den Ordner des geklonten Repositorys aus.
   - Warte, bis die anfängliche Gradle-Synchronisation (*Gradle Sync*) abgeschlossen ist.

3. **Erstellen und ausführen**:
   - Schließe ein Android-Gerät an oder starte einen Emulator (API 28+).
   - Klicke auf **Run** (`Shift + F10`).

### 📄 Lizenz

Dieses Projekt ist unter der **Apache License 2.0** lizenziert – siehe die Datei [LICENSE](LICENSE) für Details.

### ⚖️ Haftungsausschluss

KeepNotes ist eine unabhängige Anwendung und steht in keiner Verbindung zu Nextcloud GmbH, wird von dieser weder gesponsert noch unterstützt. Nextcloud ist eine eingetragene Marke der Nextcloud GmbH.
