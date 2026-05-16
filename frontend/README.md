# Booking Frontend (Angular)

## Установка и запуск

### 1. Установка зависимостей
```bash
cd frontend
npm install
```

### 2. Запуск dev сервера
```bash
npm start
```
Откройте http://localhost:4200

### 3. Сборка для production
```bash
npm run build
```

## Структура проекта

```
src/
├── app/
│   ├── components/          # Компоненты
│   │   ├── auth/           # Авторизация (login, register)
│   │   ├── home/           # Главная страница
│   │   ├── locations/      # Локации
│   │   ├── workplaces/      # Рабочие места
│   │   ├── bookings/        # Бронирования
│   │   ├── profile/        # Профиль
│   │   └── admin/          # Админ-панель
│   ├── services/           # API сервисы
│   ├── guards/             # Защита маршрутов
│   ├── interceptors/        # HTTP перехватчики
│   └── models/             # TypeScript модели
└── styles.css              # Глобальные стили
```

## API Endpoints

### Публичные
- `GET /api/locations` - список локаций
- `GET /api/locations/{id}` - локация
- `GET /api/workplaces` - рабочие места
- `GET /api/workplaces/location/{id}` - места локации
- `GET /api/services` - доп. услуги

### Авторизованные (нужен JWT токен)
- `POST /api/auth/login` - вход
- `POST /api/auth/registration` - регистрация
- `GET /api/bookings/my` - мои бронирования
- `GET /api/clients/me` - мой профиль

### Только ADMIN
- `GET /api/admin/users/active` - список пользователей
- `POST /api/admin/users/{id}/disable` - заблокировать
- `POST /api/admin/users/{id}/enable` - разблокировать

## Роли

- `ROLE_USER` - клиент (бронирования, профиль)
- `ROLE_MANAGER` - менеджер (локации, места, услуги, контракты)
- `ROLE_ADMIN` - администратор (полный доступ)
