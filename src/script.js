const splashMessages = [
  "Scrambled!",
  "Egg-cellent recipes!",
  "sudo rm -rf",
  "Too much code!",
  "Give me an A!",
  "Look Mark, I made an egg!",
  "On Guard!",
  "Buy an egg!",
  "I'm feeling tremendous!",
  "ryzen's special!"
];
// whered my splash texts go i have to remake them :(
const randomIndex = Math.floor(Math.random() * splashMessages.length);
const selectedSplash = splashMessages[randomIndex];

const splashElement = document.querySelector('.splash-text');
if (splashElement) {
  splashElement.textContent = selectedSplash;
}