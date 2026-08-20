import notionLogo from "../../assets/Notion.svg";

export default function NotionIcon({ className = "h-9 w-9" }) {
  return <img src={notionLogo} alt="Notion" className={className} />;
}
