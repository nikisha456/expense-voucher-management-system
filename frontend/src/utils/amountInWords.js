const ONES = [
  'Zero', 'One', 'Two', 'Three', 'Four', 'Five', 'Six', 'Seven', 'Eight', 'Nine', 'Ten',
  'Eleven', 'Twelve', 'Thirteen', 'Fourteen', 'Fifteen', 'Sixteen', 'Seventeen', 'Eighteen', 'Nineteen',
];
const TENS = ['', '', 'Twenty', 'Thirty', 'Forty', 'Fifty', 'Sixty', 'Seventy', 'Eighty', 'Ninety'];

function convert(number) {
  if (number < 20) return ONES[number];
  if (number < 100) return TENS[Math.floor(number / 10)] + (number % 10 ? ` ${ONES[number % 10]}` : '');
  if (number < 1000) return `${convert(Math.floor(number / 100))} Hundred${number % 100 ? ` ${convert(number % 100)}` : ''}`;
  if (number < 100000) return `${convert(Math.floor(number / 1000))} Thousand${number % 1000 ? ` ${convert(number % 1000)}` : ''}`;
  if (number < 10000000) return `${convert(Math.floor(number / 100000))} Lakh${number % 100000 ? ` ${convert(number % 100000)}` : ''}`;
  return `${convert(Math.floor(number / 10000000))} Crore${number % 10000000 ? ` ${convert(number % 10000000)}` : ''}`;
}

export function amountInWords(value) {
  if (value === '' || value === null || value === undefined || Number.isNaN(Number(value))) return '';
  const scaled = Number(value).toFixed(2);
  const [rupeePart, paisePart] = scaled.split('.');
  const rupees = Number(rupeePart);
  const paise = Number(paisePart);
  let words = `Rupees ${rupees === 0 ? 'Zero' : convert(rupees)}`;
  if (paise > 0) words += ` and ${convert(paise)} Paise`;
  return `${words} Only`;
}
