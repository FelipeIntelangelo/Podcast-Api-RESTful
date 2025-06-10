# 🎧 Podcast API

Una API RESTful para gestionar una plataforma de podcasts. Permite a los usuarios registrarse, escuchar episodios, calificarlos, marcarlos como favoritos, dejar comentarios y más. Este backend está pensado para ser consumido por un cliente (como una app móvil o frontend web), pero actualmente **no incluye interfaz visual**.

---

## 🚀 Tecnologías

- **Lenguaje:** Node.js / Python / Java (dependiendo del stack que uses)
- **Base de datos:** MySQL / MariaDB (basado en el diagrama)
- **ORM recomendado:** Sequelize / SQLAlchemy / Hibernate

---

## 📂 Estructura del modelo

### 👤 `users`
Contiene la información básica del usuario.

- `name`, `last_name`, `nickname`, `email`, `username`
- `bio`, `profile_picture`
- `password` (encriptado)
- `reset_token` (para recuperación de cuenta)

### 🗂 `podcasts`
Representa cada podcast creado por un usuario.

- `title`, `description`, `image_url`
- `is_active`: indica si está publicado
- Relación con `users` (user_id)

### 📚 `categoriesxpodcasts`
Tabla intermedia para categorizar podcasts.

- `podcast_id`, `category` (ENUM)

### 🎙 `episodes`
Episodios dentro de un podcast.

- `title`, `description`, `duration`, `audio_path`, `image_url`
- `publication_date`, `average_rating`, `views`
- Relación con `podcasts`

### 🧾 `episode_history`
Historial de reproducción del usuario.

- `episode_id`, `user_id`
- `listened_at`, `rated_at`, `rating`

### 💬 `commentaries`
Comentarios de usuarios en episodios.

- `user_id`, `episode_id`, `content`

### ❤️ `favorites`
Relación entre usuarios y podcasts que les gustan.

- `user_id`, `podcast_id`

### 🔐 `user_roles`
Tabla para gestión de roles (admin, editor, user, etc.).

---

## 📡 Endpoints (ejemplos)

> ⚠️ Todos los endpoints están en formato RESTful y devuelven JSON.

### Autenticación
- `POST /auth/register` → Crea usuario
- `POST /auth/login` → Login y JWT
- `POST /auth/recover` → Solicitar reset de contraseña

### Usuarios
- `GET /users/:id` → Info pública
- `PUT /users/:id` → Editar perfil
- `GET /users/:id/favorites` → Ver favoritos

### Podcasts
- `GET /podcasts` → Listado general
- `POST /podcasts` → Crear nuevo
- `GET /podcasts/:id` → Detalle
- `PUT /podcasts/:id` → Editar
- `DELETE /podcasts/:id` → Eliminar

### Episodios
- `GET /episodes/:id` → Info de episodio
- `POST /episodes` → Crear
- `GET /podcasts/:id/episodes` → Todos los episodios del podcast

### Comentarios
- `POST /episodes/:id/comments` → Agregar comentario
- `GET /episodes/:id/comments` → Ver comentarios

### Historial y rating
- `POST /episodes/:id/history` → Marcar como escuchado
- `PUT /episodes/:id/rate` → Puntuar episodio

---

## 🛠 Instalación

```bash
git clone https://github.com/FelipeIntelangelo/podcast.git
cd podcast-api
npm install
npm run dev
